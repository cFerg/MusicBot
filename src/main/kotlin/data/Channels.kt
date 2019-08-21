package data

import me.aberrantfox.kjdautils.api.annotation.Data

@Data("config/channels.json")
data class Channels(var channelPairings: MutableList<Pair<String, MutableList<ChannelPair>>> = mutableListOf()) {
    fun getGuild(guildID: String): MutableList<ChannelPair> {
        for (i in channelPairings){
            if (i.first == guildID){
                return i.second
            }
        }

        return mutableListOf()
    }

    fun getVoiceChannel(guildID: String, textChannelID: String) = getGuild(guildID).first {
        it.textChannelID == textChannelID
    }.voiceChannelID

    fun hasVoiceChannel(guildID: String, voiceChannelID: String) = getGuild(guildID).any {
        it.voiceChannelID == voiceChannelID
    }

    fun getTextChannel(guildID: String, voiceChannelID: String) = getGuild(guildID).first {
        it.voiceChannelID == voiceChannelID
    }.textChannelID

    fun hasTextChannel(guildID: String, textChannelID: String) = getGuild(guildID).any {
        it.textChannelID == textChannelID
    }
}

data class ChannelPair(var voiceChannelID: String = "insert-voice-ID", var textChannelID: String = "insert-channel-ID")