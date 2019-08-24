package listeners

import com.google.common.eventbus.Subscribe
import net.dv8tion.jda.api.events.guild.GuildBanEvent

class BanListener {
    @Subscribe
    fun onGuildBan(event: GuildBanEvent) {
        //TODO use event.user and event.guild to check queues by player on ban - remove them
        //TODO log that the songs were removed
    }
}
