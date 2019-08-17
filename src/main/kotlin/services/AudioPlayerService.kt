package services

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import me.aberrantfox.kjdautils.api.annotation.Service
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingDeque

@Service
class AudioPlayerService {
    var queue: BlockingQueue<AudioTrack> = LinkedBlockingDeque()
    var playerManager: AudioPlayerManager = DefaultAudioPlayerManager()
    var player: AudioPlayer
    var audioEventService: AudioEventService

    init {
        AudioSourceManagers.registerRemoteSources(playerManager)
        AudioSourceManagers.registerLocalSource(playerManager)

        player = playerManager.createPlayer()

        audioEventService = AudioEventService(this)

        player.addListener(audioEventService)
    }

    fun queue(track: AudioTrack) {
        if (!player.startTrack(track, true)) {
            println("queue trigger")
            queue.offer(track)
        }
    }

    fun startNextTrack(noInterrupt: Boolean) {
        val next = queue.poll()

        if (next != null) {
            player.startTrack(next, noInterrupt)
            println("next track add trigger")
            //currentChannel?.sendMessage("${next.info.title} by ${next.info.author} has started playing!")?.queue()
        }
    }
}