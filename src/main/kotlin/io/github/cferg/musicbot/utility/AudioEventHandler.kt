package io.github.cferg.musicbot.utility

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.*
import io.github.cferg.musicbot.extensions.*
import net.dv8tion.jda.api.entities.Guild

class AudioEventHandler(private val guild: Guild) : AudioEventAdapter() {
    override fun onPlayerPause(player: AudioPlayer) = Unit
    override fun onPlayerResume(player: AudioPlayer) = Unit

    override fun onTrackStart(player: AudioPlayer, track: AudioTrack) {
        val firstSong = guild.fetchNextSong() ?: return
        val textChannel = guild.getTextChannelById(firstSong.channelID)!!

        textChannel.sendMessage(displayTrackEmbed(guild)).queue()
    }

    override fun onTrackEnd(player: AudioPlayer, track: AudioTrack, endReason: AudioTrackEndReason) {
        if (endReason.mayStartNext || endReason == AudioTrackEndReason.FINISHED) {
            guild.nextSong()
        }
    }

    override fun onTrackException(player: AudioPlayer, track: AudioTrack, exception: FriendlyException) {
        guild.nextSong()
    }

    override fun onTrackStuck(player: AudioPlayer, track: AudioTrack, thresholdMs: Long) {
        guild.nextSong()
    }
}