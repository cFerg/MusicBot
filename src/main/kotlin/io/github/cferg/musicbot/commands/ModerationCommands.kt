package io.github.cferg.musicbot.commands

import io.github.cferg.musicbot.data.Configuration
import io.github.cferg.musicbot.services.AudioPlayerService
import me.aberrantfox.kjdautils.api.dsl.CommandSet
import me.aberrantfox.kjdautils.api.dsl.arg
import me.aberrantfox.kjdautils.api.dsl.commands
import me.aberrantfox.kjdautils.extensions.jda.sendPrivateMessage
import me.aberrantfox.kjdautils.internal.arguments.IntegerRangeArg
import me.aberrantfox.kjdautils.internal.arguments.MemberArg
import me.aberrantfox.kjdautils.internal.di.PersistenceService
import net.dv8tion.jda.api.entities.Member

@CommandSet("Moderation")
fun moderationCommands(audioPlayerService: AudioPlayerService, config: Configuration, persistenceService: PersistenceService) = commands {
    command("Restart") {
        description = "Replays the current song from the beginning."
        requiresGuild = true
        execute {
            val guild = it.guild!!
            val guildAudio = audioPlayerService.guildAudioMap[guild.id] ?: return@execute it.respond("Issue running Restart command.")
            guildAudio.player.playingTrack.position = 0
            it.respond("Restarted the song: ${guildAudio.player.playingTrack.info.title}")
        }
    }

    command("Clear") {
        description = "Removes all currently queued songs."
        requiresGuild = true
        execute {
            val guild = it.guild!!
            val guildAudio = audioPlayerService.guildAudioMap[guild.id] ?: return@execute it.respond("Issue running Clear command.")
            guildAudio.songQueue.clear()
            it.respond("Cleared the current list of songs.")
        }
    }

    command("Volume") {
        description = "Adjust volume from range 0-100"
        requiresGuild = true
        expect(IntegerRangeArg(min = 0, max = 100))
        execute {
            val guild = it.guild!!
            val guildAudio = audioPlayerService.guildAudioMap[guild.id] ?: return@execute it.respond("Issue running Volume command.")
            guildAudio.player.volume = it.args.component1() as Int
            it.respond("Set player volume to ${guildAudio.player.volume}")
        }
    }

    var previousVolume: MutableMap<String, Int> = mutableMapOf()

    command("Mute") {
        description = "Mute bot, but keeps it playing."
        requiresGuild = true
        expect(arg(MemberArg, true))
        execute {
            val guild = it.guild!!
            if (previousVolume.containsKey(guild.id)) {
                it.respond("The bot is already muted.")
            } else {
                val guildAudio = audioPlayerService.guildAudioMap[guild.id] ?: return@execute it.respond("Issue running Volume command.")
                previousVolume[guild.id] = guildAudio.player.volume
                audioPlayerService.guildAudioMap[guild.id]?.player?.volume = 0
                it.respond("The bot is now muted.")
            }
        }
    }

    command("Unmute") {
        description = "Sets bot's volume back to previous level before it was muted."
        requiresGuild = true
        expect(arg(MemberArg, true))
        execute {
            val guild = it.guild!!
            if (previousVolume.containsKey(guild.id)) {
                val guildAudio = audioPlayerService.guildAudioMap[guild.id] ?: return@execute it.respond("Issue running Unmute command.")
                guildAudio.player.volume = previousVolume[guild.id] ?: 30 //If an error occurs, default volume to 30 (change to configurable later)
                previousVolume.remove(guild.id)
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
            val guild = it.guild!!
            val member = it.args.component1() as Member
            val guildConfig = config.guildConfigurations[guild.id] ?: return@execute it.respond("Issue retrieving configurations.")

            if (guildConfig.ignoreList.contains(member.id)) {
                it.author.sendPrivateMessage("${member.effectiveName} is already in the bot blacklist.")
                return@execute
            }

            guildConfig.ignoreList.add(member.id)
            persistenceService.save(config)
            it.author.sendPrivateMessage("${member.effectiveName} is now added to the bot blacklist.")
        }
    }

    command("Unignore") {
        description = "Removes the member from a bot blacklist."
        requiresGuild = true
        expect(arg(MemberArg, false))
        execute {
            val guild = it.guild!!
            val member = it.args.component1() as Member
            val guildConfig = config.guildConfigurations[guild.id] ?: return@execute it.respond("Issue retrieving configurations.")

            if (guildConfig.ignoreList.contains(member.id)) {
                guildConfig.ignoreList.remove(member.id)
                persistenceService.save(config)
                it.author.sendPrivateMessage("${member.effectiveName} is now removed from the bot blacklist.")
            } else {
                it.author.sendPrivateMessage("${member.effectiveName} is not currently in the bot blacklist.")
            }
        }
    }
}