package io.github.cferg.musicbot.commands

import io.github.cferg.musicbot.data.Configuration
import io.github.cferg.musicbot.data.GuildInfo
import io.github.cferg.musicbot.services.AudioPlayerService
import me.aberrantfox.kjdautils.api.dsl.*
import me.aberrantfox.kjdautils.internal.arguments.*
import me.aberrantfox.kjdautils.internal.di.PersistenceService
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.TextChannel

@CommandSet("Management")
fun managementCommands(audioPlayerService: AudioPlayerService, config: Configuration, persistenceService: PersistenceService) = commands {
    command("Disconnect") {
        description = "Remove the bot from its current voice channel."
        requiresGuild = true
        execute {
            val guild = it.guild!!
            val wasDisconnected = audioPlayerService.disconnect(guild)

            if (!wasDisconnected)
                it.respond("Unable to disconnect.")
        }
    }

    command("SetStaff") {
        description = "Sets a Staff role for moderation commands"
        requiresGuild = true
        expect(RoleArg("Role"))
        execute {
            val role = it.args.component1() as Role
            val guild = it.guild!!
            val guildConfig = config.guildConfigurations[guild.id]

            guildConfig!!.staffRole = role.id
            persistenceService.save(config)
            it.respond("Assigned the Staff Role to ${role.name}")
        }
    }

    command("SetLogging") {
        description = "Sets a Logging Channel to send bot command invokes to."
        requiresGuild = true
        expect(TextChannelArg)
        execute {
            val channel = it.args.component1() as TextChannel
            val guild = it.guild!!
            val guildConfig = config.guildConfigurations[guild.id]

            guildConfig!!.loggingChannelID = channel.id
            persistenceService.save(config)
            it.respond("Assigned the Logging Channel to ${channel.name}")
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

            it.respond("Assigned the Logging Channel to ${channel.name}")
            it.respond("Assigned the Staff Role to ${role.name}")
        }
    }

    command("Prefix") {
        description = "Sets the prefix for the bot."
        requiresGuild = true
        expect(CharArg)
        execute {
            config.prefix = it.args.component1() as String
        }
    }
}