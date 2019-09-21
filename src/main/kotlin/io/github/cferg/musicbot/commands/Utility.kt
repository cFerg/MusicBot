package io.github.cferg.musicbot.commands

import io.github.cferg.musicbot.services.InfoService
import me.aberrantfox.kjdautils.api.dsl.*

@CommandSet("Utility")
fun utilityCommands(infoService: InfoService) = commands {
    command("Ping") {
        description = "Displays network ping of the bot!"
        execute {
            it.respond("Pong: ${it.discord.jda.gatewayPing}ms")
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
        requiresGuild = true
        execute {
            it.respond(infoService.botInfo(it.guild!!))
        }
    }
}