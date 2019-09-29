package io.github.cferg.musicbot.commands

import io.github.cferg.musicbot.data.Configuration
import io.github.cferg.musicbot.extensions.*
import me.aberrantfox.kjdautils.api.dsl.*
import me.aberrantfox.kjdautils.extensions.jda.toMember
import me.aberrantfox.kjdautils.internal.arguments.*
import me.aberrantfox.kjdautils.internal.di.PersistenceService
import net.dv8tion.jda.api.entities.*

@CommandSet("Moderation")
fun moderationCommands(config: Configuration, persistenceService: PersistenceService) = commands {
    command("Hoist") {
        description = "Force the song to play, pushing the rest back one in queue."
        requiresGuild = true
        expect(UrlArg)
        execute {
            val url = it.args.component1() as String
            val guild = it.guild!!
            val channel = it.channel as TextChannel
            val member = it.author.toMember(guild)!!

            guild.playSong(config, member, channel, url, multiSearch = true, noInterrupt = false)
        }
    }

    command("Restart") {
        description = "Replays the current song from the beginning."
        requiresGuild = true
        execute {
            val guild = it.guild!!
            if(!guild.restartTrack()){
                it.respond("No song currently queued.")
            }
        }
    }

    command("Clear") {
        description = "Removes all currently queued songs."
        requiresGuild = true
        execute {
            val guild = it.guild!!
            guild.clear()
        }
    }

    command("Volume") {
        description = "Adjust volume from range 0-100"
        requiresGuild = true
        expect(IntegerRangeArg(min = 0, max = 100))
        execute {
            val targetVolume = it.args.component1() as Int
            val guild = it.guild!!

            guild.setPlayerVolume(targetVolume)
            it.respond("Volume set to $targetVolume")
        }
    }

    command("Mute") {
        description = "Mute bot, but keeps it playing."
        requiresGuild = true
        execute {
            val guild = it.guild!!

            if (guild.isMuted())
                return@execute it.respond("The bot is already muted.")

            guild.mutePlayingTrack()
            it.respond("The bot is now muted.")
        }
    }

    command("Unmute") {
        description = "Sets bot's volume back to previous level before it was muted."
        requiresGuild = true
        execute {
            val guild = it.guild!!

            if (!guild.isMuted())
                return@execute it.respond("The bot is not muted.")

            guild.unmutePlayingTrack()
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
            guild.clearByMember(member.id)
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

            val wasRemoved = guildConfig.ignoreList.remove(member.id)

            val response =
                if (wasRemoved) {
                    persistenceService.save(config)
                    "${member.effectiveName} is now removed from the bot blacklist."
                } else {
                    "${member.effectiveName} is not currently in the bot blacklist."
                }

            it.respond(response)
        }
    }
}