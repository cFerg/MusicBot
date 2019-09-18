package io.github.cferg.musicbot.commands

import io.github.cferg.musicbot.services.InfoService
import me.aberrantfox.kjdautils.api.dsl.*
import java.awt.Color

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

    command("ListCommands") {
        requiresGuild = true
        description = "Lists all available commands."
        execute {
            val commands = it.container.commands.values.groupBy { it.category }.toList()
                    .sortedBy { (_, value) -> -value.size }.toMap()

            it.respond(embed {
                commands.forEach {
                    field {
                        name = it.key
                        value = it.value.sortedBy { it.name.length }.joinToString("\n") { it.name }
                        inline = true
                    }
                }
                color = Color.green
            })
        }
    }
}