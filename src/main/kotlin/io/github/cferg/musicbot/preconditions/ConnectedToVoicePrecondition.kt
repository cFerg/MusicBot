package io.github.cferg.musicbot.preconditions

import io.github.cferg.musicbot.utility.Constants
import me.aberrantfox.kjdautils.api.dsl.*
import me.aberrantfox.kjdautils.internal.command.*

@Precondition
fun isConnectedToVoiceChannel() = exit@{ event: CommandEvent ->
    event.guild ?: return@exit Fail("This command must be executed in a text channel.")

    val command = event.container.commands[event.commandStruct.commandName] ?: return@exit Pass

    if (command.category == Constants.MODERATION_CATEGORY ||
        command.category == Constants.MANAGEMENT_CATEGORY ||
        command.category == Constants.UTILITY_CATEGORY) return@exit Pass

    event.message.member!!.voiceState?.channel
        ?: return@exit Fail("Please join a voice channel to use this command.")

    return@exit Pass
}
