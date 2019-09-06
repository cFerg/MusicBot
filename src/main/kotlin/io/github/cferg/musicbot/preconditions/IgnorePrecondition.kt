package io.github.cferg.musicbot.preconditions

import io.github.cferg.musicbot.data.Configuration
import me.aberrantfox.kjdautils.api.dsl.*
import me.aberrantfox.kjdautils.internal.command.*
import net.dv8tion.jda.api.entities.TextChannel

@Precondition
fun isIgnored(config: Configuration) = exit@{ event: CommandEvent ->
    if (event.channel !is TextChannel) return@exit Fail("**Failure:** This command must be executed in a text channel.")

    if (!config.guildConfigurations[event.guild!!.id]!!.ignoreList.contains(event.author.id)) return@exit Pass

    return@exit Fail("You are currently blacklisted form using this bot's commands.")
}