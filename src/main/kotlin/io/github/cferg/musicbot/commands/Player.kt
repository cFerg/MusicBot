package io.github.cferg.musicbot.commands

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import io.github.cferg.musicbot.data.Configuration
import io.github.cferg.musicbot.extensions.toTimeString
import me.aberrantfox.kjdautils.api.dsl.CommandSet
import me.aberrantfox.kjdautils.api.dsl.arg
import me.aberrantfox.kjdautils.api.dsl.commands
import me.aberrantfox.kjdautils.internal.arguments.IntegerRangeArg
import me.aberrantfox.kjdautils.internal.arguments.UrlArg
import io.github.cferg.musicbot.services.AudioPlayerService
import io.github.cferg.musicbot.services.AudioPlayerService.*
import io.github.cferg.musicbot.services.EmbedTrackListService
import me.aberrantfox.kjdautils.extensions.jda.sendPrivateMessage
import me.aberrantfox.kjdautils.extensions.jda.toMember
import me.aberrantfox.kjdautils.internal.arguments.MemberArg
import me.aberrantfox.kjdautils.internal.di.PersistenceService
import net.dv8tion.jda.api.entities.Member

@CommandSet("Player")
fun playerCommands(plugin: AudioPlayerService, config: Configuration, persistenceService: PersistenceService, embed: EmbedTrackListService) = commands {
    //TODO add add as an alias of the command with an argument
    command("Play") {
        description = "Play the song listed - If a song is already playing, it's added to a queue."
        requiresGuild = true
        expect(arg(UrlArg))
        execute {
            val url = it.args.component1() as String
            val guild = it.guild!!
            val vc = it.author.toMember(guild)!!.voiceState?.channel
                    ?: return@execute it.respond("Please join a voice channel to use this command.")

            plugin.audioManagers[guild.id]?.openAudioConnection(vc)

            plugin.playerManager[guild.id]?.loadItem(url, object : AudioLoadResultHandler {
                override fun trackLoaded(track: AudioTrack) {
                    //TODO add track length check
                    plugin.queueAdd(guild.id, Song(track, it.author.id, guild.id, it.channel.id))
                    it.message.delete().complete()
                }

                override fun playlistLoaded(playlist: AudioPlaylist) {
                    //TODO add permission check for individuals to play playlists
                    //TODO add track length check - using configurable minimum and max range
                    for (track in playlist.tracks) {
                        plugin.queueAdd(guild.id, Song(track, it.author.id, guild.id, it.channel.id))
                    }
                    it.message.delete().complete()
                }

                override fun noMatches() = it.respond("No matching song found")

                override fun loadFailed(throwable: FriendlyException) = it.respond("Error, could not load track.")
            })
        }
    }

    command("Pause") {
        description = "Pauses the current song."
        requiresGuild = true
        execute {
            val music = plugin.player[it.guild!!.id]!!

            if (music.isPaused) {
                it.respond("Player is already paused.")
            } else {
                music.isPaused = true

                if (music.playingTrack != null) {
                    val duration = music.playingTrack.position.toTimeString()
                    it.respond("Paused song: ${music.playingTrack.info.title} at $duration")
                } else {
                    it.respond("Player is paused, but no songs are currently queued.")
                }
            }
        }
    }

    //TODO add play with no arguments as an invoker
    command("Resume") {
        description = "Continues the last song (If one is still queued)"
        requiresGuild = true
        execute {
            val music = plugin.player[it.guild!!.id]!!

            if (!music.isPaused) {
                if (music.playingTrack != null) {
                    it.respond("The song is already playing.")
                } else {
                    it.respond("No songs are currently queued.")
                }
            } else {
                music.isPaused = false

                if (music.playingTrack != null) {
                    val duration = music.playingTrack.position.toTimeString()
                    it.respond("Resumed song: ${music.playingTrack.info.title} from $duration")
                } else {
                    it.respond("Player is resumed, but no songs are currently queued.")
                }
            }
        }
    }

    //TODO add next as an alias
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

    command("Ignore") {
        description = "Add the member to a bot blacklist."
        requiresGuild = true
        expect(arg(MemberArg, false))
        execute {
            val member = it.args.component1() as Member

            if (config.guildConfigurations[it.guild!!.id]!!.ignoreList.contains(member.id)) {
                it.author.sendPrivateMessage("${member.effectiveName} is already in the bot blacklist.")
                return@execute
            }

            config.guildConfigurations[it.guild!!.id]!!.ignoreList.add(member.id)
            persistenceService.save(config)
            it.author.sendPrivateMessage("${member.effectiveName} is now added to the bot blacklist.")
        }
    }

    command("Unignore") {
        description = "Removes the member from a bot blacklist."
        requiresGuild = true
        expect(arg(MemberArg, false))
        execute {
            val member = it.args.component1() as Member

            if (config.guildConfigurations[it.guild!!.id]!!.ignoreList.contains(member.id)) {
                config.guildConfigurations[it.guild!!.id]!!.ignoreList.remove(member.id)
                persistenceService.save(config)
                it.author.sendPrivateMessage("${member.effectiveName} is now removed from the bot blacklist.")
            } else {
                it.author.sendPrivateMessage("${member.effectiveName} is not currently in the bot blacklist.")
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