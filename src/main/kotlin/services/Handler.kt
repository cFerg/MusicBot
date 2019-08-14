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
            println("queue trigger")
            queue.offer(track)
        }
    }

    fun clearQueue() = queue.clear()

    fun skip(){
        println("skip trigger")
        currentChannel?.sendMessage("${plugin.player.playingTrack.info.title} has been skipped!")?.queue()
        plugin.player.startTrack(queue.poll(), false)
    }

    private fun startNextTrack(noInterrupt: Boolean) {
        val next = queue.poll()

        if (next != null) {
            if (!plugin.player.startTrack(next, noInterrupt)) {
                println("next track add trigger")
                queue.add(next)
                currentChannel?.sendMessage("${next.info.title} by ${next.info.author} has started playing!")?.queue()
            }else{
                println("next track put trigger")
                queue.put(next)
                currentChannel?.sendMessage("${next.info.title} by ${next.info.author} has started playing!")?.queue()
            }
        } else {
            plugin.player.stopTrack()
        }
    }

    override fun onPlayerPause(player:AudioPlayer) {
        println("Player Pause Trigger")
        currentChannel?.sendMessage("${player.playingTrack.info.title} is now paused.")?.queue()
    }

    override fun onPlayerResume(player:AudioPlayer) {
        println("Player Resume Trigger")
        currentChannel?.sendMessage("${player.playingTrack.info.title} resumed playing.")?.queue()
    }

    override fun onTrackStart(player:AudioPlayer, track:AudioTrack) {
        println("Track Start Trigger")
        currentChannel?.sendMessage("${track.info.title} started playing.")?.queue()
    }

    override fun onTrackEnd(player:AudioPlayer, track:AudioTrack, endReason: AudioTrackEndReason) {
        if (endReason.mayStartNext || endReason == AudioTrackEndReason.FINISHED) {
            println("Track End Trigger")
            startNextTrack(true)
        }
    }

    override fun onTrackException(player:AudioPlayer, track:AudioTrack, exception: FriendlyException) {
        currentChannel?.sendMessage("\\!\\!\\! Error Occurred - Please Let Staff Know \\!\\!\\!")?.queue()
    }

    override fun onTrackStuck(player:AudioPlayer, track:AudioTrack, thresholdMs:Long) {
        currentChannel?.sendMessage("${track.info.title} is stuck - Skipping the song!")?.queue()
        startNextTrack(false)
    }
}