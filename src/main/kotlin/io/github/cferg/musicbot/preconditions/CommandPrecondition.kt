package io.github.cferg.musicbot.preconditions

import io.github.cferg.musicbot.data.Configuration
import io.github.cferg.musicbot.utility.Constants
import me.aberrantfox.kjdautils.api.dsl.*
import me.aberrantfox.kjdautils.extensions.jda.*
import me.aberrantfox.kjdautils.internal.command.*
import net.dv8tion.jda.api.entities.TextChannel
import java.awt.Color

@Precondition(priority = 4)
fun getCommand(config: Configuration) = precondition{ event: CommandEvent ->
    with(event) {
        if (event.channel !is TextChannel) return@precondition Fail("**Failure:** This command must be executed in a text channel.")
        val guild = guild ?: return@precondition Fail("**Failure:** This command must be ran in a guild.")
        val user = event.author.toMember(guild) ?: return@precondition Fail("**Failure:** Only users can type commands.")

        val command = event.container.commands[event.commandStruct.commandName] ?: return@precondition Pass

        when (command.category) {
            Constants.UTILITY_CATEGORY -> return@precondition Pass //No need to log these commands

            Constants.MANAGEMENT_CATEGORY -> {
                if (!user.isOwner) return@precondition Fail("Only the owner can type these commands.")

                return@precondition Pass //No need to log these commands
            }

            Constants.MODERATION_CATEGORY -> {
                val staffRole = config.guildConfigurations[guild.id]?.staffRole
                val isStaff = user.roles.any { staff -> staff.id == staffRole }

                if (!isStaff) return@precondition Fail("Only staff can type these commands.")
            }
        }

        val guildConfig = config.guildConfigurations[guild.id] ?: return@precondition Pass
        val textChannel = guild.getTextChannelById(guildConfig.loggingChannelID) ?: return@precondition Pass

        val argList = buildString {
            event.commandStruct.commandArgs.forEach { arg ->
                append("$arg ")
            }
        }

        val display = embed {
            color = Color(0xFFDA33)
            addField("${author.fullName()} | ID: ${author.idLong} | invoked command:",
                    "${commandStruct.commandName} $argList in <#${channel.idLong}>")
        }

        textChannel.sendMessage(display).queue()

        return@precondition Pass
    }
}