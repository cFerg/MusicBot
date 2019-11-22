package io.github.cferg.musicbot.commands

import io.github.cferg.musicbot.utility.botInfo
import io.github.cferg.musicbot.utility.source
import me.aberrantfox.kjdautils.api.dsl.command.*

@CommandSet("Utility")
fun utilityCommands() = commands {
    command("Ping") {
        description = "Displays network ping of the bot!"
        execute {
            it.discord.jda.restPing.queue{ ping ->
                it.respond("Pong: ${ping}ms")
            }
        }
    }

    command("Repo", "Source") {
        description = "Display the (source code) repository link."
        execute {
            it.respond(source)
        }
    }

    command("About", "Info") {
        description = "Displays the bot information."
        execute {
            it.respond(botInfo(it.guild!!))
        }
    }
}