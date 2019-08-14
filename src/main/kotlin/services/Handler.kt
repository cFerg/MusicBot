package services

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import data.currentChannel
import me.aberrantfox.kjdautils.api.annotation.Service
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.BlockingQueue

@Service
class Handler(private val plugin:ManagerService) : AudioEventAdapter() {
    private var queue: BlockingQueue<AudioTrack> = LinkedBlockingDeque()

    fun isQueueEmpty():Boolean = queue.isNullOrEmpty()

    fun queue(track:AudioTrack){
        if (!plugin.player.startTrack(track, true)) {
            queue.offer(track)
        }
    }

    fun clearQueue() = queue.clear()

    fun skip(){
        currentChannel?.sendMessage("${plugin.player.playingTrack.info.title} has been skipped!")
        plugin.player.startTrack(queue.poll(), false)
    }

    private fun startNextTrack(noInterrupt: Boolean) {
        val next = queue.poll()

        if (next != null) {
            if (!plugin.player.startTrack(next, noInterrupt)) {
                queue.add(next)
                currentChannel?.sendMessage("${next.info.title} by ${next.info.author} has started playing!")
            }else{
                queue.put(next)
                currentChannel?.sendMessage("${next.info.title} by ${next.info.author} has started playing!")
            }
        } else {
            plugin.player.stopTrack()
        }
    }

    override fun onPlayerPause(player:AudioPlayer) {
        currentChannel?.sendMessage("${player.playingTrack.info.title} is now paused.")
    }

    override fun onPlayerResume(player:AudioPlayer) {
        currentChannel?.sendMessage("${player.playingTrack.info.title} resumed playing.")
    }

    override fun onTrackStart(player:AudioPlayer, track:AudioTrack) {
        currentChannel?.sendMessage("${track.info.title} started playing.")
    }

    override fun onTrackEnd(player:AudioPlayer, track:AudioTrack, endReason: AudioTrackEndReason) {
        if (endReason.mayStartNext || endReason == AudioTrackEndReason.FINISHED) {
            startNextTrack(true)
        }
    }

    override fun onTrackException(player:AudioPlayer, track:AudioTrack, exception: FriendlyException) {
        currentChannel?.sendMessage("\\!\\!\\! Error Occurred - Please Let Staff Know \\!\\!\\!")
    }

    override fun onTrackStuck(player:AudioPlayer, track:AudioTrack, thresholdMs:Long) {
        currentChannel?.sendMessage("${track.info.title} is stuck - Skipping the song!")
        startNextTrack(false)
    }
}