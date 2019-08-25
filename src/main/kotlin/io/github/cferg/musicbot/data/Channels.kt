package io.github.cferg.musicbot.data

import me.aberrantfox.kjdautils.api.annotation.Data

@Data("config/channels.json")
data class Channels(var channelGroups: MutableMap<String, ChannelGroup> = mutableMapOf()) {
    fun getVoicePair(guildID: String) = channelGroups[guildID]!!.voicePair

    fun getLoggingChannel(guildID: String) = channelGroups[guildID]!!.loggingChannelID

    fun getVoiceChannel(guildID: String, textChannelID: String) = getVoicePair(guildID).first {
        it.textChannelID == textChannelID
    }.voiceChannelID

    fun hasVoiceChannel(guildID: String, voiceChannelID: String) = getVoicePair(guildID).any {
        it.voiceChannelID == voiceChannelID
    }

    fun getTextChannel(guildID: String, voiceChannelID: String) = getVoicePair(guildID).first {
        it.voiceChannelID == voiceChannelID
    }.textChannelID

    fun hasTextChannel(guildID: String, textChannelID: String) = getVoicePair(guildID).any {
        it.textChannelID == textChannelID
    }
}

data class VoicePair(var voiceChannelID: String = "insert-voice-ID", var textChannelID: String = "insert-channel-ID")

data class ChannelGroup(var loggingChannelID: String = "insert-channel-ID", var voicePair: MutableList<VoicePair>)