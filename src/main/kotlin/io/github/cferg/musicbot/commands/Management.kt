package io.github.cferg.musicbot.commands

import io.github.cferg.musicbot.data.*
import io.github.cferg.musicbot.extensions.disconnect
import io.github.cferg.musicbot.extensions.toTimeString
import me.aberrantfox.kjdautils.api.dsl.*
import me.aberrantfox.kjdautils.internal.arguments.*
import me.aberrantfox.kjdautils.internal.di.PersistenceService
import net.dv8tion.jda.api.entities.*

@CommandSet("Management")
fun managementCommands(config: Configuration, persistenceService: PersistenceService) = commands {
    command("Disconnect") {
        description = "Remove the bot from its current voice channel."
        requiresGuild = true
        execute {
            val guild = it.guild!!
            guild.disconnect()
        }
    }

    command("StaffRole") {
        description = "Sets a Staff role for moderation commands"
        requiresGuild = true
        expect(RoleArg("Role"))
        execute {
            val role = it.args.component1() as Role
            val guild = it.guild!!
            val guildConfig = config.guildConfigurations[guild.id]!!

            guildConfig.staffRole = role.id
            persistenceService.save(config)
            it.respond("Assigned the Staff Role to ${role.name}")
        }
    }

    //TODO add an ability to remove the role | everyone doesn't work
    command("PlaylistRole") {
        description = "Sets a role for the ability to add playlist"
        requiresGuild = true
        expect(RoleArg("Role"))
        execute {
            val role = it.args.component1() as Role
            val guild = it.guild!!
            val guildConfig = config.guildConfigurations[guild.id]!!

            guildConfig.playlistRole = role.id
            persistenceService.save(config)
            it.respond("Assigned the Playlist Role to ${role.name}")
        }
    }

    command("Logging") {
        description = "Sets a Logging Channel to send bot command invokes to."
        requiresGuild = true
        expect(TextChannelArg)
        execute {
            val channel = it.args.component1() as TextChannel
            val guild = it.guild!!
            val guildConfig = config.guildConfigurations[guild.id]!!

            guildConfig.loggingChannelID = channel.id
            persistenceService.save(config)
            it.respond("Assigned the Logging Channel to ${channel.name}")
        }
    }

    command("PlaylistLimit") {
        description = "Sets a maximum playlist song limit | Set to 0 for no limits."
        requiresGuild = true
        expect(IntegerArg("Song Limit"))
        execute {
            val limit = it.args.component1() as Int
            val guild = it.guild!!
            val guildConfig = config.guildConfigurations[guild.id]!!

            guildConfig.playlistQueueLimit = limit
            persistenceService.save(config)
            it.respond("Set the playlist song queue limit to $limit")
        }
    }

    command("SongDuration") {
        description = "Sets a maximum song duration limit | Set to 0 for no limits."
        requiresGuild = true
        expect(TimeStringArg("Time"))
        execute {
            val time = it.args.component1() as Double
            val guild = it.guild!!
            val guildConfig = config.guildConfigurations[guild.id]!!
            guildConfig.songMaxDuration = time.toLong() * 1000
            val newTime = guildConfig.songMaxDuration
            persistenceService.save(config)
            it.respond("Set the song max time limit to ${newTime.toTimeString()}")
        }
    }

    command("SongLimit") {
        description = "Sets how many songs a person can queue at a given time | Set to 0 for no limits."
        requiresGuild = true
        expect(IntegerArg("Song Limit"))
        execute {
            val limit = it.args.component1() as Int
            val guild = it.guild!!
            val guildConfig = config.guildConfigurations[guild.id]!!

            guildConfig.songQueueLimit = limit
            persistenceService.save(config)
            it.respond("Set the song queue limit to $limit")
        }
    }

    command("Setup") {
        description = "Setups the configuration for a guild."
        requiresGuild = true
        expect(RoleArg("Role"), TextChannelArg)
        execute {
            val role = it.args.component1() as Role
            val channel = it.args.component2() as TextChannel
            val guild = it.guild!!
            val guildConfig = config.guildConfigurations

            if (guild.id in guildConfig){
                val currentConfig = guildConfig[guild.id]!!
                currentConfig.staffRole = role.id
                currentConfig.loggingChannelID = channel.id
                persistenceService.save(config)
            }else{
                config.guildConfigurations[guild.id] = GuildInfo(role.id, channel.id)
                persistenceService.save(config)
            }

            it.respond("Assigned the Staff Role to ${role.name}")
            it.respond("Assigned the Logging Channel to ${channel.name}")
        }
    }

    command("Prefix") {
        description = "Sets the prefix for the bot."
        requiresGuild = true
        expect(CharArg)
        execute {
            config.prefix = it.args.component1() as String
            persistenceService.save(config)
        }
    }
}