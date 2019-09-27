package io.github.cferg.musicbot.preconditions

import io.github.cferg.musicbot.utility.Constants
import me.aberrantfox.kjdautils.api.dsl.*
import me.aberrantfox.kjdautils.internal.command.*

@Precondition(2)
fun isConnectedToVoiceChannel() = exit@{ event: CommandEvent ->
    event.guild ?: return@exit Fail("This command must be executed in a text channel.")

    val command = event.container.commands[event.commandStruct.commandName] ?: return@exit Pass

    val passCategories = listOf(Constants.MODERATION_CATEGORY, Constants.MANAGEMENT_CATEGORY, Constants.UTILITY_CATEGORY)
    
    if (command.category in passCategories) return@exit Pass

    event.message.member!!.voiceState?.channel
        ?: return@exit Fail("Please join a voice channel to use this command.")

    return@exit Pass
}
