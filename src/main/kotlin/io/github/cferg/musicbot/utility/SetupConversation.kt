package io.github.cferg.musicbot.utility

import io.github.cferg.musicbot.data.Configuration
import io.github.cferg.musicbot.data.GuildInfo
import me.aberrantfox.kjdautils.api.dsl.Convo
import me.aberrantfox.kjdautils.api.dsl.conversation
import me.aberrantfox.kjdautils.internal.arguments.RoleArg
import me.aberrantfox.kjdautils.internal.arguments.TextChannelArg
import me.aberrantfox.kjdautils.internal.di.PersistenceService

@Convo
fun setupConversation(config: Configuration, persistenceService: PersistenceService) = conversation(name = "setupConversation") {
    val guild = this.guild
    val guildConfig = config.guildConfigurations

    val role = blockingPrompt(RoleArg) { configurationNeeded(guild,
        "Please enter a **Role ID** used for Staff Permissions.",
        "If you have to, you may create a Role now, then type the ID here.")
    }

    val channel = blockingPrompt(TextChannelArg) { configurationNeeded(guild,
        "Please enter a **Text Channel ID** used for Logging.",
        "If you have to, you may create a Text Channel now, then type the ID here.")
    }

    if (guild.id in guildConfig){
        val currentConfig = guildConfig[guild.id]!!
        currentConfig.staffRole = role.id
        currentConfig.loggingChannelID = channel.id
        persistenceService.save(config)

        guild.getTextChannelById(currentConfig.loggingChannelID)!!.sendMessage("Successfully Setup Guild Configurations").queue()
    }else{
        config.guildConfigurations[guild.id] = GuildInfo(role.id, channel.id)
        persistenceService.save(config)

        guild.getTextChannelById(guildConfig[guild.id]!!.loggingChannelID)!!.sendMessage("Successfully Setup Guild Configurations").queue()
    }

    val pr = this.discord.configuration.prefix

    respond(configurationSuccessful(
            "For more Management Configuration options, type **${pr}${pr}help** within **${guild.name}**\n",
            "**Here are some recommended commands:**",
            "**SetPlaylistRole** - Assign a role for adding playlists **(**Default: Everyone**)**",
            "**SetPlaylistLimit** - Set the song count limit within a playlist. **(**Default: No Limit**)**",
            "**SetSongDuration** - Configure song length **(**Default: No Limit**)**",
            "**SetSongLimit** - Configure song limit per person within the queue (Default: No Limit)"))
}