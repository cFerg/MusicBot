package io.github.cferg.musicbot.listeners

import com.google.common.eventbus.Subscribe
import me.aberrantfox.kjdautils.discord.Discord
import net.dv8tion.jda.api.events.guild.GuildJoinEvent

class GuildJoinListener(private val discord: Discord) {
    @Subscribe
    fun onGuildJoin(event: GuildJoinEvent) {
        discord.getUserById(event.guild.ownerId)!!.sendPrivateMessage("Please run the Setup command for ${event.guild.name}.\n" +
                "You'll need to provide:\n" +
                "A Role's Name for Staff Perms\n" +
                "A Text Channel ID for a Logging Channel\n")
    }
}