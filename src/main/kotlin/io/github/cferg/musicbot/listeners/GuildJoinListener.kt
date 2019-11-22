package io.github.cferg.musicbot.listeners

import com.google.common.eventbus.Subscribe
import me.aberrantfox.kjdautils.internal.services.ConversationService
import net.dv8tion.jda.api.events.guild.GuildJoinEvent

class GuildJoinListener(private val conversationService: ConversationService) {
    @Subscribe
    fun onGuildJoin(event: GuildJoinEvent) {
        val guild = event.guild
        val owner = guild.owner?.user ?: return

        conversationService.createConversation(owner, guild, "setupConversation")
    }
}