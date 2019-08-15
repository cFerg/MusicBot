package services

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import me.aberrantfox.kjdautils.api.annotation.Service
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.BlockingQueue

@Service
class Handler(private val plugin: ManagerService) : AudioEventAdapter() {
    var queue: BlockingQueue<AudioTrack> = LinkedBlockingDeque()

    fun queue(track: AudioTrack) {
        if (!plugin.player.startTrack(track, true)) {
            println("queue trigger")
            queue.offer(track)
        }
    }

    private fun startNextTrack(noInterrupt: Boolean) {
        val next = queue.poll()

        if (next != null) {
            plugin.player.startTrack(next, noInterrupt)
            println("next track add trigger")
            //currentChannel?.sendMessage("${next.info.title} by ${next.info.author} has started playing!")?.queue()
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
            startNextTrack(true)
        }
    }

    override fun onTrackException(player: AudioPlayer, track: AudioTrack, exception: FriendlyException) {
        //currentChannel?.sendMessage("\\!\\!\\! Error Occurred - Please Let Staff Know \\!\\!\\!")?.queue()
    }

    override fun onTrackStuck(player: AudioPlayer, track: AudioTrack, thresholdMs: Long) {
        //currentChannel?.sendMessage("${track.info.title} is stuck - Skipping the song!")?.queue()
        startNextTrack(false)
    }
}