package data

import me.aberrantfox.kjdautils.api.annotation.Data

@Data("config/channels.json")
data class Channels(var channelPairings: MutableList<ChannelPair> = mutableListOf(ChannelPair())){
    fun getVoiceChannel(textChannelID: String) = channelPairings.first {
        it.textChannelID == textChannelID
    }.voiceChannelID

    fun getTextChannel(voiceChannelID: String) = channelPairings.first {
        it.voiceChannelID == voiceChannelID
    }.textChannelID

    fun hasVoiceChannel(voiceChannelID: String) = channelPairings.any {
        it.voiceChannelID == voiceChannelID
    }

    fun hasTextChannel(textChannelID: String) = channelPairings.any {
        it.textChannelID == textChannelID
    }
}

data class ChannelPair(var voiceChannelID: String = "insert-voice-ID", var textChannelID: String = "insert-channel-ID")