package io.github.cferg.musicbot.preconditions

import io.github.cferg.musicbot.utility.Constants
import me.aberrantfox.kjdautils.api.dsl.*
import me.aberrantfox.kjdautils.api.dsl.command.CommandEvent
import me.aberrantfox.kjdautils.internal.command.*

@Precondition(priority = 2)
fun isConnectedToVoiceChannel() = precondition { event: CommandEvent<*> ->
    event.guild ?: return@precondition Fail("This command must be executed in a text channel.")

    val command = event.container.commands[event.commandStruct.commandName] ?: return@precondition Pass
    
    if (command.category != Constants.PLAYER_CATEGORY) return@precondition Pass

    event.message.member!!.voiceState?.channel
        ?: return@precondition Fail("Please join a voice channel to use this command.")

    return@precondition Pass
}
