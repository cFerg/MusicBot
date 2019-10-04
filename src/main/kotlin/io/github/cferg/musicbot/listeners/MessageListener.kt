package io.github.cferg.musicbot.listeners

import com.google.common.eventbus.Subscribe
import io.github.cferg.musicbot.services.InfoService
import me.aberrantfox.kjdautils.extensions.stdlib.trimToID
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

//TODO remove when this is added to kUtils
class MessageListener(private val infoService: InfoService) {
    @Subscribe
    fun onMessageReceived(event: GuildMessageReceivedEvent) {
        with(event) {
            if (author.isBot) return

            if (message.contentRaw.trimToID() == channel.jda.selfUser.id)
                channel.sendMessage(infoService.botInfo(guild)).queue()
        }
    }
}