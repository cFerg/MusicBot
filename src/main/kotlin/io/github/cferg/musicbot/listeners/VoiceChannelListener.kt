package io.github.cferg.musicbot.listeners

import com.google.common.eventbus.Subscribe
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent

class VoiceChannelListener {
    private val botConnection: MutableMap<String, Boolean> = mutableMapOf()

    fun isConnected(guildID: String): Boolean {
        return if (botConnection.containsKey(guildID)){
            botConnection[guildID]!!
        }else{
            botConnection.putIfAbsent(guildID, false)
            false
        }
    }

    @Subscribe
    fun onGuildVoiceJoin(event: GuildVoiceJoinEvent) {
        if (event.member == event.jda.selfUser){
            botConnection[event.guild.id] = true
        }
    }

    @Subscribe
    fun onGuildVoiceLeave(event: GuildVoiceLeaveEvent) {
        if (event.member == event.jda.selfUser){
            botConnection[event.guild.id] = false
        }
    }
}
