package io.github.cferg.musicbot.listeners

import com.google.common.eventbus.Subscribe
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import io.github.cferg.musicbot.services.InfoService

//TODO remove when this is added to kUtils
class MessageListener(private val infoService: InfoService) {
    @Subscribe
    fun onMessageReceived(event: GuildMessageReceivedEvent) {
        with (event) {
            if (author.isBot) return

            if (message.contentRaw == jda.selfUser.asMention)
                channel.sendMessage(infoService.botInfo(guild)).queue()
        }
    }
}