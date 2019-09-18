package io.github.cferg.musicbot.preconditions

import io.github.cferg.musicbot.data.Configuration
import io.github.cferg.musicbot.utility.Constants
import me.aberrantfox.kjdautils.api.dsl.CommandEvent
import me.aberrantfox.kjdautils.api.dsl.Precondition
import me.aberrantfox.kjdautils.api.dsl.embed
import me.aberrantfox.kjdautils.extensions.jda.fullName
import me.aberrantfox.kjdautils.extensions.jda.toMember
import me.aberrantfox.kjdautils.internal.command.Fail
import me.aberrantfox.kjdautils.internal.command.Pass
import net.dv8tion.jda.api.entities.TextChannel
import java.awt.Color

@Precondition(4)
fun getCommand(config: Configuration) = exit@{ event: CommandEvent ->
    with(event) {
        if (event.channel !is TextChannel) return@exit Fail("**Failure:** This command must be executed in a text channel.")
        val guild = guild ?: return@exit Fail("**Failure:** This command must be ran in a guild.")
        val user = event.author.toMember(guild) ?: return@exit Fail("**Failure:** Only users can type commands.")

        val command = event.container.commands[event.commandStruct.commandName] ?: return@exit Pass

        if (command.category == Constants.UTILITY_CATEGORY) return@exit Pass //No need to log these commands

        if (command.category == Constants.MANAGEMENT_CATEGORY){
            if (!user.isOwner) return@exit Fail("Only the owner can type these commands.")

            return@exit Pass //No need to log these commands
        }

        if (command.category == Constants.MODERATION_CATEGORY){
            val staffRole = config.guildConfigurations[guild.id]?.staffRole
            val isStaff = user.roles.any { staff -> staff.id == staffRole }

            if (!isStaff) return@exit Fail("Only staff can type these commands.")
        }

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