package io.github.cferg.musicbot.services

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import me.aberrantfox.kjdautils.api.annotation.Service

@Service
class AudioEventService(private val audioPlayerService: AudioPlayerService) : AudioEventAdapter() {
    override fun onPlayerPause(player: AudioPlayer) = Unit
    override fun onPlayerResume(player: AudioPlayer) = Unit
    override fun onTrackStart(player: AudioPlayer, track: AudioTrack) = Unit

    override fun onTrackEnd(player: AudioPlayer, track: AudioTrack, endReason: AudioTrackEndReason) {
        if (endReason.mayStartNext || endReason == AudioTrackEndReason.FINISHED) {
            val guildID = audioPlayerService.trackGuildMap[track.identifier]!!

            audioPlayerService.nextSong(guildID)
        }
    }

    override fun onTrackException(player: AudioPlayer, track: AudioTrack, exception: FriendlyException) {
        val guildID = audioPlayerService.trackGuildMap[track.identifier]!!

        audioPlayerService.nextSong(guildID)
    }

    override fun onTrackStuck(player: AudioPlayer, track: AudioTrack, thresholdMs: Long) {
        val guildID = audioPlayerService.trackGuildMap[track.identifier]!!

        audioPlayerService.nextSong(guildID)
    }
}