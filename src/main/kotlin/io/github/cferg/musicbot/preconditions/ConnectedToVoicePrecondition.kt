package io.github.cferg.musicbot.preconditions

import io.github.cferg.musicbot.data.Configuration
import io.github.cferg.musicbot.utility.Constants
import me.aberrantfox.kjdautils.api.dsl.CommandEvent
import me.aberrantfox.kjdautils.api.dsl.Precondition
import me.aberrantfox.kjdautils.extensions.jda.toMember
import me.aberrantfox.kjdautils.internal.command.Fail
import me.aberrantfox.kjdautils.internal.command.Pass

@Precondition
fun isConnectedToVoiceChannel(config: Configuration) = exit@{ event: CommandEvent ->
    val guild = event.guild ?: return@exit Fail("This command must be executed in a text channel.")
    val command = event.container.commands[event.commandStruct.commandName] ?: return@exit Pass
    if (command.category == Constants.MODERATION_CATEGORY) return@exit Pass

    event.author.toMember(guild)!!.voiceState?.channel
            ?: return@exit Fail("Please join a voice channel to use this command.")
}
