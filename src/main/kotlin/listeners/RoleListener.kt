package listeners

import com.google.common.eventbus.Subscribe
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent
import services.AudioPlayerService

class RoleListener(private val plugin: AudioPlayerService) {
    //TODO log that the songs were removed

    @Subscribe
    fun onGuildRoleAdd(event: GuildMemberRoleAddEvent) {
        //TODO trigger song removal on blacklist (adds a bot mute role)
        //TODO add variable that they can't queue songs or use the commands
    }

    @Subscribe
    fun onGuildRoleRemove(event: GuildMemberRoleRemoveEvent){
        //TODO remove variable - allowing someone to use the commands again.
    }
}