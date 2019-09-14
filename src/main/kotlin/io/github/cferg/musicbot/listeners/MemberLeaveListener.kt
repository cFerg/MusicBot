package io.github.cferg.musicbot.listeners

import com.google.common.eventbus.Subscribe
import io.github.cferg.musicbot.data.Configuration
import io.github.cferg.musicbot.extensions.clearByMember
import me.aberrantfox.kjdautils.api.dsl.embed
import me.aberrantfox.kjdautils.extensions.jda.fullName
import net.dv8tion.jda.api.events.guild.member.GuildMemberLeaveEvent
import java.awt.Color

class MemberLeaveListener(private val configuration: Configuration) {
    @Subscribe
    fun onGuildMemberLeave(event: GuildMemberLeaveEvent) {
        val guild = event.guild
        val config = configuration.guildConfigurations[guild.id] ?: return
        val textChannel = guild.getTextChannelById(config.loggingChannelID) ?: return
        val user = event.user

        if(guild.clearByMember(user.id)) {
            val display = embed {
                color = Color.red
                addField("Cleared songs from ${user.fullName()} | ID: ${user.idLong}", "Invoked via Leave/Kick/Ban Event")
            }

            textChannel.sendMessage(display).queue()
        }
    }
}