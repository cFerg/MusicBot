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
            //TODO re-add play from here
        }
    }

    override fun onTrackException(player: AudioPlayer, track: AudioTrack, exception: FriendlyException) {
        //currentChannel?.sendMessage("\\!\\!\\! Error Occurred - Please Let Staff Know \\!\\!\\!")?.queue()
    }

    override fun onTrackStuck(player: AudioPlayer, track: AudioTrack, thresholdMs: Long) {
        //currentChannel?.sendMessage("${track.info.title} is stuck - Skipping the song!")?.queue()
        //audioPlayerService.playSong(audioPlayerService.guildAudioMap[]) .startNextTrack(plugin.currentGuild[track.identifier]!!, false)
        //TODO add a guild lookup based on track identifier
    }
}