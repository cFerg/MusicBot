package io.github.cferg.musicbot.utility

import io.github.cferg.musicbot.data.Configuration
import io.github.cferg.musicbot.data.GuildInfo
import me.aberrantfox.kjdautils.api.annotation.Service
import me.aberrantfox.kjdautils.discord.Discord
import me.aberrantfox.kjdautils.internal.di.PersistenceService

@Service
class ConfigLoader(discord: Discord, configuration: Configuration, persistenceService: PersistenceService) {
    init {
        discord.jda.guilds.forEach { guild ->
            configuration.guildConfigurations.putIfAbsent(guild.id, GuildInfo())
            persistenceService.save(configuration)
        }
    }
}