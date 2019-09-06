package io.github.cferg.musicbot.utility

import io.github.cferg.musicbot.data.Configuration
import me.aberrantfox.kjdautils.api.annotation.Service
import me.aberrantfox.kjdautils.discord.Discord

@Service
class PrefixLoader(discord: Discord, configuration: Configuration) {
    init {
        discord.configuration.prefix = configuration.prefix
    }
}