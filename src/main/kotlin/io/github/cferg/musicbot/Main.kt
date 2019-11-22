package io.github.cferg.musicbot

import io.github.cferg.musicbot.utility.botInfo
import me.aberrantfox.kjdautils.api.startBot

lateinit var botPrefix: String

fun main(args: Array<String>) {
    val token = args.firstOrNull()
        ?: return println("Missing token!")

    startBot(token) {
        configure {
            allowPrivateMessages = false
            mentionEmbed = { event -> botInfo(event.guild) }
            reactToCommands = false
        }

        botPrefix = config.prefix
    }
}