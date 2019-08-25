package io.github.cferg.musicbot.preconditions

import io.github.cferg.musicbot.data.Channels
import me.aberrantfox.kjdautils.api.dsl.CommandEvent
import me.aberrantfox.kjdautils.api.dsl.Precondition
import me.aberrantfox.kjdautils.internal.command.Fail
import me.aberrantfox.kjdautils.internal.command.Pass

private const val Category = "Player"

@Precondition
fun isMusicChannelPrecondition(channels: Channels) = exit@{ event: CommandEvent ->
    val command = event.container.commands[event.commandStruct.commandName] ?: return@exit Pass
    if (command.category != Category) return@exit Pass
    if (channels.hasTextChannel(event.guild!!.id, event.channel.id)) return@exit Pass
    return@exit Fail("Please perform this command in the appropriate channel.")
}