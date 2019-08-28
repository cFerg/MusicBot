package io.github.cferg.musicbot.preconditions

import io.github.cferg.musicbot.data.Configuration
import me.aberrantfox.kjdautils.api.dsl.CommandEvent
import me.aberrantfox.kjdautils.api.dsl.Precondition
import me.aberrantfox.kjdautils.internal.command.Fail
import me.aberrantfox.kjdautils.internal.command.Pass

@Precondition
fun isIgnored(config: Configuration) = exit@{ event: CommandEvent ->
    if (!config.guildConfigurations[event.guild!!.id]!!.ignoreList.contains(event.author.id)) return@exit Pass

    return@exit Fail("You are currently blacklisted form using this bot's commands.")
}