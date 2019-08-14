package data

import me.aberrantfox.kjdautils.api.annotation.Data
import me.aberrantfox.kjdautils.internal.arguments.VoiceChannelArg

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

val currentVoice: VoiceChannelArg? = null
