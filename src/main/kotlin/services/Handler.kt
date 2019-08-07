package services

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import plugin
import java.util.concurrent.BlockingDeque
import java.util.ArrayList
import java.util.concurrent.atomic.AtomicBoolean
import net.dv8tion.jda.core.entities.Message
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.ScheduledExecutorService

class Handler : AudioEventAdapter() {
    private val player: AudioPlayer? = null
    private val messageDispatcher: MessageDispatcher? = null
    private val executorService: ScheduledExecutorService? = null
    private val queue: BlockingDeque<AudioTrack>? = null
    private val boxMessage: AtomicReference<Message>? = null
    private val creatingBoxMessage: AtomicBoolean? = null

    //channel.sendMessage.queue()

    fun queue(track:AudioTrack){
        queue?.addLast(track)
        startNextTrack(true)
    }

    fun clearQueue(): List<AudioTrack> {
        val clearQueue = ArrayList<AudioTrack>()
        queue?.drainTo(clearQueue)
        return clearQueue
    }

    private fun startNextTrack(noInterrupt: Boolean) {
        val next = queue?.pollFirst()

        if (next != null) {
            if (!plugin.player.startTrack(next, noInterrupt)) {
                queue?.addFirst(next)
            }
        } else {
            plugin.player.stopTrack()
        }
    }

    fun skip(){
        startNextTrack(false)
    }

    override fun onPlayerPause(player:AudioPlayer) {
        updateTrackBox(false)
        // Player was paused
    }

    override fun onPlayerResume(player:AudioPlayer) {
        updateTrackBox(false)
        // Player was resumed
    }

    override fun onTrackStart(player:AudioPlayer, track:AudioTrack) {
        updateTrackBox(true)
        // A track started playing
    }

    override fun onTrackEnd(player:AudioPlayer, track:AudioTrack, endReason: AudioTrackEndReason) {
        if (endReason.mayStartNext) {
            startNextTrack(true)
            // Start next track
        }

        // endReason == FINISHED: A track finished or died by an exception (mayStartNext = true).
        // endReason == LOAD_FAILED: Loading of a track failed (mayStartNext = true).
        // endReason == STOPPED: The player was stopped.
        // endReason == REPLACED: Another track started playing while this had not finished
        // endReason == CLEANUP: Player hasn't been queried for a while, if you want you can put a
        //                       clone of this back to your queue
    }

    override fun onTrackException(player:AudioPlayer, track:AudioTrack, exception: FriendlyException) {
        // An already playing track threw an exception (track end event will still be received separately)
    }

    override fun onTrackStuck(player:AudioPlayer, track:AudioTrack, thresholdMs:Long) {
        startNextTrack(false)
        // Audio track has been unable to provide us any audio, might want to just start a new track
    }

    private fun updateTrackBox(newMessage: Boolean) {
        val track = plugin.player.playingTrack

        if (track == null || newMessage) {
            boxMessage?.getAndSet(null)?.delete()
        }

        if (track != null) {
            val message = boxMessage?.get()
            val box = TrackBoxBuilder.buildTrackBox(80, track, plugin.player.isPaused, plugin.player.volume)

            if (message != null) {
                message.editMessage(box).queue()
            } else {
                if (creatingBoxMessage?.compareAndSet(false, true)!!) {
                    messageDispatcher?.sendMessage(box)
                }
            }
        }
    }
}