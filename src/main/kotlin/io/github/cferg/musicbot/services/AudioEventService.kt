package io.github.cferg.musicbot.services

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import me.aberrantfox.kjdautils.api.annotation.Service

@Service
class AudioEventService(private val plugin: AudioPlayerService) : AudioEventAdapter() {
    var guildID: String = ""

    init {
        for (i in plugin.audioEventService){
            if (i.value == this){
                guildID = i.key
                break
            }
        }
    }

    override fun onPlayerPause(player: AudioPlayer) {
        println("Player Pause Trigger")
        //currentChannel?.sendMessage("${player.playingTrack.info.title} is now paused.")?.queue()
    }

    override fun onPlayerResume(player: AudioPlayer) {
        println("Player Resume Trigger")
        //currentChannel?.sendMessage("${player.playingTrack.info.title} resumed playing.")?.queue()
    }

    override fun onTrackStart(player: AudioPlayer, track: AudioTrack) {
        println("Track Start Trigger")
        //currentChannel?.sendMessage("${track.info.title} started playing.")?.queue()
    }

    override fun onTrackEnd(player: AudioPlayer, track: AudioTrack, endReason: AudioTrackEndReason) {
        if (endReason.mayStartNext || endReason == AudioTrackEndReason.FINISHED) {
            println("Track End Trigger")
            plugin.startNextTrack(guildID, true)
        }
    }

    override fun onTrackException(player: AudioPlayer, track: AudioTrack, exception: FriendlyException) {
        //currentChannel?.sendMessage("\\!\\!\\! Error Occurred - Please Let Staff Know \\!\\!\\!")?.queue()
    }

    override fun onTrackStuck(player: AudioPlayer, track: AudioTrack, thresholdMs: Long) {
        //currentChannel?.sendMessage("${track.info.title} is stuck - Skipping the song!")?.queue()
        plugin.startNextTrack(guildID, false)
    }
}