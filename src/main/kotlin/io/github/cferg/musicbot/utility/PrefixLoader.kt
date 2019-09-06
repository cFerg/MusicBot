package io.github.cferg.musicbot.utility

import io.github.cferg.musicbot.data.Configuration
import me.aberrantfox.kjdautils.discord.Discord

class PrefixLoader(discord: Discord, configuration: Configuration) {
    init {
        discord.configuration.prefix = configuration.prefix
    }
}