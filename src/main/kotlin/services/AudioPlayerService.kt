package services

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import data.Channels
import me.aberrantfox.kjdautils.api.annotation.Service
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingDeque

@Service
class AudioPlayerService(channels: Channels) {
    var queue: MutableMap<String, BlockingQueue<AudioTrack>> = mutableMapOf()
    var playerManager: MutableMap<String, AudioPlayerManager> = mutableMapOf()
    var player: MutableMap<String, AudioPlayer> = mutableMapOf()
    var audioEventService: MutableMap<String, AudioEventService> = mutableMapOf()

    init {
        for (i in channels.channelPairings) {
            queue[i.first] = LinkedBlockingDeque()
            playerManager[i.first] = DefaultAudioPlayerManager()

            AudioSourceManagers.registerRemoteSources(playerManager[i.first])
            AudioSourceManagers.registerLocalSource(playerManager[i.first])

            player[i.first] = playerManager[i.first]!!.createPlayer()
            player[i.first]!!.volume = 30
            audioEventService[i.first] = AudioEventService(this)
            player[i.first]!!.addListener(audioEventService[i.first])
        }
    }

    fun queueAdd(guildID: String, track: AudioTrack) {
        if (!player[guildID]!!.startTrack(track, true)) {
            queue[guildID]!!.offer(track)
        }
    }

    fun startNextTrack(guildID: String, noInterrupt: Boolean) {
        val next = queue[guildID]!!.poll()

        if (next != null) {
            player[guildID]!!.startTrack(next, noInterrupt)
            //currentChannel?.sendMessage("${next.info.title} by ${next.info.author} has started playing!")?.queue()
        }else{
            player[guildID]!!.stopTrack()
        }
    }
}