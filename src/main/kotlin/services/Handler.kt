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
class Handler(private val plugin:ManagerService) : AudioEventAdapter() {
    private var queue: BlockingQueue<AudioTrack> = LinkedBlockingDeque<AudioTrack>()

    fun isQueueEmpty():Boolean = queue.isNullOrEmpty()

    fun queue(track:AudioTrack){
        if (!plugin.player.startTrack(track, true)) {
            queue.offer(track)
        }
    }

    fun clearQueue() = queue.clear()

    fun skip(){
        plugin.player.startTrack(queue.poll(), false)
    }

    private fun startNextTrack(noInterrupt: Boolean) {
        val next = queue.poll()

        if (next != null) {
            if (!plugin.player.startTrack(next, noInterrupt)) {
                queue.add(next)
                println("Song added")
            }else{
                queue.put(next)
                println("Song put")
            }
        } else {
            plugin.player.stopTrack()
        }
    }

    override fun onPlayerPause(player:AudioPlayer) {
        // Player was paused
    }

    override fun onPlayerResume(player:AudioPlayer) {
        // Player was resumed
    }

    override fun onTrackStart(player:AudioPlayer, track:AudioTrack) {
        // A track started playing
        //TODO add a config file, and store and pull associated text channel
        println("${track.info.title} started playing")
    }

    override fun onTrackEnd(player:AudioPlayer, track:AudioTrack, endReason: AudioTrackEndReason) {
        if (endReason.mayStartNext || endReason == AudioTrackEndReason.FINISHED) {
            startNextTrack(true)
        }

        // endReason == FINISHED: A track finished or died by an exception (mayStartNext = true).
        // endReason == LOAD_FAILED: Loading of a track failed (mayStartNext = true).
        // endReason == STOPPED: The player was stopped.
        // endReason == REPLACED: Another track started playing while this had not finished
        // endReason == CLEANUP: Player hasn't been queried for a while, if you want you can put a clone of this back to your queue
    }

    override fun onTrackException(player:AudioPlayer, track:AudioTrack, exception: FriendlyException) {
        // An already playing track threw an exception (track end event will still be received separately)
    }

    override fun onTrackStuck(player:AudioPlayer, track:AudioTrack, thresholdMs:Long) {
        startNextTrack(false)
        // Audio track has been unable to provide us any audio, might want to just start a new track
    }
}