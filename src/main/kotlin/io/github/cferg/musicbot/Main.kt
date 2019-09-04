package io.github.cferg.musicbot

import me.aberrantfox.kjdautils.api.startBot

fun main(args: Array<String>) {
    val token = args.firstOrNull()
        ?: return println("Missing token!")

    startBot(token) {
        configure {
            globalPath = "io.github.cferg.musicbot"

            container.commands.getValue("help").category = "Utility"
        }
    }
}