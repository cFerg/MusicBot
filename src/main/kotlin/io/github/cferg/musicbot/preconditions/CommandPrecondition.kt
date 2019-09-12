package io.github.cferg.musicbot.preconditions

import io.github.cferg.musicbot.data.Configuration
import me.aberrantfox.kjdautils.api.dsl.CommandEvent
import me.aberrantfox.kjdautils.api.dsl.Precondition
import me.aberrantfox.kjdautils.extensions.jda.fullName
import me.aberrantfox.kjdautils.internal.command.Fail
import me.aberrantfox.kjdautils.internal.command.Pass
import net.dv8tion.jda.api.entities.TextChannel

@Precondition(4)
fun isCommand(config: Configuration) = exit@{ event: CommandEvent ->
    with(event) {
        if (event.channel !is TextChannel) return@exit Fail("**Failure:** This command must be executed in a text channel.")
        val guild = guild ?: return@exit Fail("**Failure:** This command must be ran in a guild.")
        val guildConfig = config.guildConfigurations[guild.id] ?: return@exit Pass
        val textChannel = guild.getTextChannelById(guildConfig.loggingChannelID) ?: return@exit Pass

        textChannel.sendMessage("${author.fullName()} | ID: ${author.idLong} | invoked `${commandStruct.commandName}` in <#${channel.idLong}>").queue()

        return@exit Pass
    }
}