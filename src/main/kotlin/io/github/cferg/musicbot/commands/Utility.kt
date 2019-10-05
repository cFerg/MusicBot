package io.github.cferg.musicbot.commands

import io.github.cferg.musicbot.services.InfoService
import me.aberrantfox.kjdautils.api.dsl.*

@CommandSet("Utility")
fun utilityCommands(infoService: InfoService) = commands {
    command("Ping") {
        description = "Displays network ping of the bot!"
        execute {
            it.discord.jda.restPing.queue{ ping ->
                it.respond("Pong: ${ping}ms")
            }
        }
    }

    command("Source") {
        description = "Display the (source code) repository link."
        execute {
            it.respond(infoService.source)
        }
    }

    command("BotInfo") {
        description = "Displays the bot information."
        execute {
            it.respond(infoService.botInfo(it.guild!!))
        }
    }
}