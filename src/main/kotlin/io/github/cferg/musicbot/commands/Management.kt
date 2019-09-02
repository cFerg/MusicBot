package io.github.cferg.musicbot.commands

import io.github.cferg.musicbot.data.Configuration
import io.github.cferg.musicbot.services.AudioPlayerService
import me.aberrantfox.kjdautils.api.dsl.CommandSet
import me.aberrantfox.kjdautils.api.dsl.commands
import me.aberrantfox.kjdautils.internal.arguments.CharArg
import me.aberrantfox.kjdautils.internal.arguments.RoleArg
import me.aberrantfox.kjdautils.internal.di.PersistenceService
import net.dv8tion.jda.api.entities.Role

@CommandSet("Management")
fun managementCommands(audioPlayerService: AudioPlayerService, config: Configuration, persistenceService: PersistenceService) = commands {
    command("Disconnect") {
        description = "Remove the bot from its current voice channel."
        requiresGuild = true
        execute {
            val guild = it.guild!!
            guild.audioManager.closeAudioConnection()
            audioPlayerService.guildAudioMap[guild.id]!!.player.stopTrack()
        }
    }

    command("SetStaff") {
        description = "Sets a Staff role for moderation commands"
        requiresGuild = true
        expect(RoleArg("Role"))
        execute {
            val role = it.args.component1() as Role
            config.guildConfigurations[it.guild!!.id]!!.staffRole = role.id
            persistenceService.save(config)
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