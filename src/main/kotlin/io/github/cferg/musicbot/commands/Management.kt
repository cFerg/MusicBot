package io.github.cferg.musicbot.commands

import io.github.cferg.musicbot.data.*
import io.github.cferg.musicbot.extensions.*
import me.aberrantfox.kjdautils.api.dsl.command.*
import me.aberrantfox.kjdautils.internal.arguments.*
import me.aberrantfox.kjdautils.internal.di.PersistenceService

@CommandSet("Management")
fun managementCommands(config: Configuration, persistenceService: PersistenceService) = commands {
    command("Disconnect") {
        description = "Remove the bot from its current voice channel."
        execute {
            val guild = it.guild!!
            guild.disconnect()
        }
    }

    command("StaffRole") {
        description = "Sets a Staff role for moderation commands"
        execute(RoleArg("Role Name")) {
            val (role) = it.args
            val guild = it.guild!!
            val guildConfig = config.guildConfigurations[guild.id]!!

            guildConfig.staffRole = role.id
            persistenceService.save(config)
            it.respond("Assigned the Staff Role to ${role.name}")
        }
    }

    command("PlaylistRole") {
        description = "Sets a role on who can add playlists"
        execute(RoleArg("Role Name")) {
            val guild = it.guild!!
            val guildConfig = config.guildConfigurations[guild.id]!!

            val (role) = it.args
            guildConfig.playlistRole = role.id
            it.respond("Assigned the Playlist Role to ${role.name}")

            persistenceService.save(config)
        }
    }

    command("PlaylistRoleClear") {
        description = "Removes a role, letting everyone add playlists."
        execute {
            val guild = it.guild!!
            val guildConfig = config.guildConfigurations[guild.id]!!

            guildConfig.playlistRole = ""
            it.respond("Assigned the Playlist Role to everyone.")
            persistenceService.save(config)
        }
    }

    command("Logging") {
        description = "Sets a Logging Channel to send bot command invokes to."
        execute(TextChannelArg("Text Channel Name")) {
            val (channel) = it.args
            val guild = it.guild!!
            val guildConfig = config.guildConfigurations[guild.id]!!

            guildConfig.loggingChannelID = channel.id
            persistenceService.save(config)
            it.respond("Assigned the Logging Channel to ${channel.name}")
        }
    }

    command("PlaylistLimit") {
        description = "Sets a maximum playlist song limit | Set to 0 for no limits."
        execute(IntegerArg("Song Limit")) {
            val (limit) = it.args
            val guild = it.guild!!
            val guildConfig = config.guildConfigurations[guild.id]!!

            guildConfig.playlistQueueLimit = limit
            persistenceService.save(config)
            it.respond("Set the playlist song queue limit to $limit")
        }
    }

    command("SongDuration") {
        description = "Sets a maximum song duration limit | Set to 0 for no limits."
        execute(TimeStringArg("Time")) {
            val (time) = it.args
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
        execute(IntegerArg("Song Limit")) {
            val (limit) = it.args
            val guild = it.guild!!
            val guildConfig = config.guildConfigurations[guild.id]!!

            guildConfig.songQueueLimit = limit
            persistenceService.save(config)
            it.respond("Set the song queue limit to $limit")
        }
    }

    command("Setup") {
        description = "Setups the configuration for a guild."
        execute(RoleArg("Role Name"), TextChannelArg("Text Channel Name")) {
            val (role, channel) = it.args
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
        execute(CharArg("Prefix Character")) {
            val (letter) = it.args
            config.prefix = letter.toString()
            persistenceService.save(config)
        }
    }
}