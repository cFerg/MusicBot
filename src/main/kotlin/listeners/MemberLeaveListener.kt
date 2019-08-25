package listeners

import com.google.common.eventbus.Subscribe
import net.dv8tion.jda.api.events.guild.GuildBanEvent
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberLeaveEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent
import services.AudioPlayerService

class MemberLeaveListener(private val plugin: AudioPlayerService) {
    //TODO log that the songs were removed

    @Subscribe
    fun onGuildMemberLeave(event: GuildMemberLeaveEvent){
        plugin.clearByMember(event.guild.id,event.user.discriminator)
    }
}
