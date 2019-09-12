package io.github.cferg.musicbot.listeners

import com.google.common.eventbus.Subscribe
import io.github.cferg.musicbot.extensions.clearByMember
import net.dv8tion.jda.api.events.guild.member.GuildMemberLeaveEvent

class MemberLeaveListener {
    //TODO log that the songs were removed

    @Subscribe
    fun onGuildMemberLeave(event: GuildMemberLeaveEvent) {
        event.guild.clearByMember(event.user.id)
    }
}
