package io.github.cferg.musicbot.listeners

import net.dv8tion.jda.api.entities.VoiceChannel

class VoiceChannelListener {
    //TODO move to where it's needed or remove if unnecessary
    fun VoiceChannel.isBotConnected() = members.any { it.user == jda.selfUser }
}
