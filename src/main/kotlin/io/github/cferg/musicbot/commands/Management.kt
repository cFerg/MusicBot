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

    command("SetStaffRole") {
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

    command("SetPlaylistRole") {
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

    command("RemovePlaylistRole") {
        description = "Removes a role, letting everyone add playlists."
        execute {
            val guild = it.guild!!
            val guildConfig = config.guildConfigurations[guild.id]!!

            guildConfig.playlistRole = ""
            it.respond("Assigned the Playlist Role to everyone.")
            persistenceService.save(config)
        }
    }

    command("SetLoggingChannel") {
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

    command("SetPlaylistLimit") {
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

    command("CanReact") {
        description = "Sets whether to react to commands, with an emote."
        execute(BooleanArg) {
            val choice = it.args.first
            val guild = it.guild!!
            val guildConfig = config.guildConfigurations[guild.id]!!
            guildConfig.reactToCommands = choice
            persistenceService.save(config)
            it.respond("${if (choice) "Enabled" else "Disabled"} command reactions.")
        }
    }

    command("SetSongDuration") {
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

    command("SetSongLimit") {
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

    command("SetPrefix") {
        description = "Sets the prefix for the bot."
        execute(CharArg("Prefix Character")) {
            val (letter) = it.args
            config.prefix = letter.toString()
            persistenceService.save(config)
        }
    }
}