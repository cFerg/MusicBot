package io.github.cferg.musicbot.commands

import io.github.cferg.musicbot.data.Configuration
import io.github.cferg.musicbot.services.AudioPlayerService
import me.aberrantfox.kjdautils.api.dsl.*
import me.aberrantfox.kjdautils.extensions.jda.toMember
import me.aberrantfox.kjdautils.internal.arguments.*
import me.aberrantfox.kjdautils.internal.di.PersistenceService
import net.dv8tion.jda.api.entities.*

@CommandSet("Moderation")
fun moderationCommands(audioPlayerService: AudioPlayerService, config: Configuration, persistenceService: PersistenceService) = commands {
    command("Hoist") {
        description = "Force the song to play, pushing the rest back one in queue."
        requiresGuild = true
        expect(UrlArg)
        execute {
            val url = it.args.component1() as String
            val guild = it.guild!!
            val channel = it.channel as TextChannel
            val member = it.author.toMember(guild)!!

            audioPlayerService.playSong(guild, member.id, channel, url, false)
        }
    }

    command("Restart") {
        description = "Replays the current song from the beginning."
        requiresGuild = true
        execute {
            val guild = it.guild!!
            val guildAudio = audioPlayerService.guildAudioMap[guild.id]
                ?: return@execute it.respond("Issue running Restart command.")

            val track = guildAudio.player.playingTrack

            track.position = 0
            it.respond("Restarted the song: ${track.info.title}")
        }
    }

    command("Clear") {
        description = "Removes all currently queued songs."
        requiresGuild = true
        execute {
            val guild = it.guild!!
            audioPlayerService.clear(guild.id)
        }
    }

    command("Volume") {
        description = "Adjust volume from range 0-100"
        requiresGuild = true
        expect(IntegerRangeArg(min = 0, max = 100))
        execute {
            val guild = it.guild!!
            val guildAudio = audioPlayerService.guildAudioMap[guild.id]
                ?: return@execute it.respond("Issue running Volume command.")

            val player = guildAudio.player

            player.volume = it.args.component1() as Int
            it.respond("Set player volume to ${player.volume}")
        }
    }

    val previousVolume: MutableMap<String, Int> = mutableMapOf()

    command("Mute") {
        description = "Mute bot, but keeps it playing."
        requiresGuild = true
        execute {
            val guild = it.guild!!
            if (previousVolume.containsKey(guild.id))
                return@execute it.respond("The bot is already muted.")

            val guildAudio = audioPlayerService.guildAudioMap[guild.id]
                ?: return@execute it.respond("Issue running Volume command.")

            previousVolume[guild.id] = guildAudio.player.volume
            guildAudio.player.volume = 0
            it.respond("The bot is now muted.")
        }
    }

    command("Unmute") {
        description = "Sets bot's volume back to previous level before it was muted."
        requiresGuild = true
        execute {
            val guild = it.guild!!
            if (!previousVolume.containsKey(guild.id))
                return@execute it.respond("The bot is currently not muted - check the volume level.")

            val guildAudio = audioPlayerService.guildAudioMap[guild.id]
                ?: return@execute it.respond("Issue running Unmute command.")

            //If an error occurs, default volume to 30 (change to configurable later)
            guildAudio.player.volume = previousVolume[guild.id] ?: 30
            previousVolume.remove(guild.id)
            it.respond("The bot is now unmuted.")
        }
    }

    command("Ignore") {
        description = "Add the member to a bot blacklist."
        requiresGuild = true
        expect(MemberArg)
        execute {
            val guild = it.guild!!
            val member = it.args.component1() as Member
            val guildConfig = config.guildConfigurations[guild.id]
                ?: return@execute it.respond("Issue retrieving configurations.")

            if (guildConfig.ignoreList.contains(member.id)) {
                it.respond("${member.effectiveName} is already in the bot blacklist.")
                return@execute
            }

            guildConfig.ignoreList.add(member.id)
            persistenceService.save(config)
            it.respond("${member.effectiveName} is now added to the bot blacklist.")
        }
    }

    command("Unignore") {
        description = "Removes the member from a bot blacklist."
        requiresGuild = true
        expect(MemberArg)
        execute {
            val guild = it.guild!!
            val member = it.args.component1() as Member
            val guildConfig = config.guildConfigurations[guild.id]
                ?: return@execute it.respond("Issue retrieving configurations.")

            val response = if (guildConfig.ignoreList.contains(member.id)) {
                guildConfig.ignoreList.remove(member.id)
                persistenceService.save(config)
                "${member.effectiveName} is now removed from the bot blacklist."
            } else {
                "${member.effectiveName} is not currently in the bot blacklist."
            }

            it.respond(response)
        }
    }
}