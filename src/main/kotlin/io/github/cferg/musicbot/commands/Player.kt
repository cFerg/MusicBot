package io.github.cferg.musicbot.commands

import io.github.cferg.musicbot.data.Configuration
import io.github.cferg.musicbot.services.AudioPlayerService
import io.github.cferg.musicbot.services.EmbedTrackListService
import me.aberrantfox.kjdautils.api.dsl.CommandSet
import me.aberrantfox.kjdautils.api.dsl.arg
import me.aberrantfox.kjdautils.api.dsl.commands
import me.aberrantfox.kjdautils.extensions.jda.toMember
import me.aberrantfox.kjdautils.internal.arguments.IntegerRangeArg
import me.aberrantfox.kjdautils.internal.arguments.MemberArg
import me.aberrantfox.kjdautils.internal.arguments.UrlArg
import me.aberrantfox.kjdautils.internal.di.PersistenceService
import net.dv8tion.jda.api.entities.TextChannel

@CommandSet("Player")
fun playerCommands(audioPlayerService: AudioPlayerService, config: Configuration, persistenceService: PersistenceService, embed: EmbedTrackListService) = commands {
    command("Play") {
        description = "Play the song listed - If a song is already playing, it's added to a queue."
        requiresGuild = true
        expect(arg(UrlArg))
        execute {
            val url = it.args.component1() as String
            val guild = it.guild!!
            val channel = it.channel as TextChannel
            audioPlayerService.playSong(guild, it.author.toMember(guild)!!.id, channel, url)
        }
    }

    command("Skip") {
        description = "Skips the current song."
        requiresGuild = true
        execute {
            if (plugin.songQueue.isNullOrEmpty())
                return@execute it.respond("No songs currently queued.")

            val guild = it.guild!!
            val member = it.author.toMember(guild)!!
            val staffRole = config.guildConfigurations[guild.id]!!.staffRole

            val playingTrack = plugin.player[guild.id]?.playingTrack?.info
                    ?: return@execute it.respond("No songs currently queued.")

            val queuedSong = it.author.id == plugin.currentSong[guild.id]?.memberID
            val isStaff = member.roles.any { it.id == staffRole }

            if (!queuedSong && !isStaff)
                return@execute it.respond("Sorry, only the person who queued the song or staff can skip.")

            plugin.startNextTrack(guild.id, false)
            it.respond("Skipped song: ${playingTrack.title} by ${playingTrack.author}")
        }
    }

    command("Restart") {
        description = "Replays the current song from the beginning."
        requiresGuild = true
        execute {
            plugin.player[it.guild!!.id]!!.playingTrack.position = 0
            it.respond("Restarted the song: ${plugin.player[it.guild!!.id]!!.playingTrack.info.title}")
        }
    }

    command("Clear") {
        description = "Removes all currently queued songs."
        requiresGuild = true
        execute {
            plugin.songQueue.clear()
            it.respond("Cleared the current list of songs.")
        }
    }

    command("Volume") {
        description = "Adjust volume from range 0-100"
        requiresGuild = true
        expect(IntegerRangeArg(min = 0, max = 100))
        execute {
            plugin.player[it.guild!!.id]!!.volume = it.args.component1() as Int
        }
    }

    var previousVolume: MutableMap<String, Int> = mutableMapOf()

    command("Mute") {
        description = "Mute bot, but keeps it playing."
        requiresGuild = true
        expect(arg(MemberArg, true))
        execute {
            if (previousVolume.containsKey(it.guild!!.id)) {
                it.respond("The bot is already muted.")
            } else {
                previousVolume[it.guild!!.id] = plugin.player[it.guild!!.id]!!.volume
                plugin.player[it.guild!!.id]!!.volume = 0
                it.respond("The bot is now muted.")
            }
        }
    }

    command("Unmute") {
        description = "Sets bot's volume back to previous level before it was muted."
        requiresGuild = true
        expect(arg(MemberArg, true))
        execute {
            if (previousVolume.containsKey(it.guild!!.id)) {
                plugin.player[it.guild!!.id]!!.volume = previousVolume[it.guild!!.id]!!
                previousVolume.remove(it.guild!!.id)
                it.respond("The bot is now unmuted.")
            } else {
                it.respond("The bot is currently not muted - check the volume level.")
            }
        }
    }

    command("List") {
        description = "Lists the current songs."
        requiresGuild = true
        execute {
            it.respond(embed.trackDisplay(it.guild!!, plugin))
        }
    }
}