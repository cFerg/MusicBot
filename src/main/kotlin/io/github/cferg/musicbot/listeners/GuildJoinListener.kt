package io.github.cferg.musicbot.listeners

import com.google.common.eventbus.Subscribe
import me.aberrantfox.kjdautils.extensions.jda.sendPrivateMessage
import net.dv8tion.jda.api.events.guild.GuildJoinEvent

class GuildJoinListener() {
    @Subscribe
    fun onGuildJoin(event: GuildJoinEvent) {
        val guild = event.guild
        val owner = guild.owner?.user ?: return

        owner.sendPrivateMessage("Please run the Setup command for ${guild.name}.\n" +
                "You'll need to provide:\n" +
                "A Role's Name for Staff Perms\n" +
                "A Text Channel ID for a Logging Channel\n")
    }
}