package data

import me.aberrantfox.kjdautils.api.annotation.Data
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.VoiceChannel

@Data("config/channels.json")
data class Channels(var channelPairings: MutableList<ChannelPair> = mutableListOf(ChannelPair())){
    fun getVoiceChannel(channelID: String) = channelPairings.first {
        it.channelID == channelID
    }.voiceID

    fun getTextChannel(voiceID: String) = channelPairings.first {
        it.voiceID == voiceID
    }.channelID
}

data class ChannelPair(var voiceID: String = "insert-voice-ID", var channelID: String = "insert-channel-ID")

var currentVoice: VoiceChannel? = null
var currentChannel: TextChannel? = null
