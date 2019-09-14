package io.github.cferg.musicbot.preconditions

import io.github.cferg.musicbot.data.Configuration
import me.aberrantfox.kjdautils.api.dsl.CommandEvent
import me.aberrantfox.kjdautils.api.dsl.Precondition
import me.aberrantfox.kjdautils.api.dsl.embed
import me.aberrantfox.kjdautils.extensions.jda.fullName
import me.aberrantfox.kjdautils.internal.command.Fail
import me.aberrantfox.kjdautils.internal.command.Pass
import net.dv8tion.jda.api.entities.TextChannel
import java.awt.Color

@Precondition(4)
fun isCommand(config: Configuration) = exit@{ event: CommandEvent ->
    with(event) {
        if (event.channel !is TextChannel) return@exit Fail("**Failure:** This command must be executed in a text channel.")
        val guild = guild ?: return@exit Fail("**Failure:** This command must be ran in a guild.")
        val guildConfig = config.guildConfigurations[guild.id] ?: return@exit Pass
        val textChannel = guild.getTextChannelById(guildConfig.loggingChannelID) ?: return@exit Pass

        var argList = ""

        for (i in event.commandStruct.commandArgs) {
            argList += "$i "
        }

        val display = embed {
            color = Color.yellow
            addField("${author.fullName()} | ID: ${author.idLong} | invoked command:",
                    "${commandStruct.commandName} $argList in <#${channel.idLong}>")
        }

        textChannel.sendMessage(display).queue()

        return@exit Pass
    }
}