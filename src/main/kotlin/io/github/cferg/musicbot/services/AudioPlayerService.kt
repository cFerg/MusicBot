package io.github.cferg.musicbot.services

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import me.aberrantfox.kjdautils.api.annotation.Service
import me.aberrantfox.kjdautils.discord.Discord

@Service
class AudioPlayerService(discord: Discord) {
    var songQueue: MutableMap<String, MutableList<Song>> = mutableMapOf()
    var playerManager: MutableMap<String, AudioPlayerManager> = mutableMapOf()
    var player: MutableMap<String, AudioPlayer> = mutableMapOf()
    var audioEventService: MutableMap<String, AudioEventService> = mutableMapOf()

    init {
        discord.jda.guilds.map { it.id }.forEach { id ->
            songQueue[id] = ArrayList()
            playerManager[id] = DefaultAudioPlayerManager()

            AudioSourceManagers.registerRemoteSources(playerManager[id])
            AudioSourceManagers.registerLocalSource(playerManager[id])

            player[id] = playerManager[id]!!.createPlayer()
            player[id]!!.volume = 30
            audioEventService[id] = AudioEventService(this)
            player[id]!!.addListener(audioEventService[id])
        }
    }

    data class Song(val track: AudioTrack, val memberID: String)

    fun queueAdd(guildID: String, song: Song) {
        if (!player[guildID]!!.startTrack(song.track, true)) {
            songQueue[guildID]!!.add(song)
        }
    }

    fun clearByMember(guildID: String, memberID: String){
        for (i in songQueue[guildID]!!){
            if(i.memberID == memberID){
                songQueue[guildID]!!.remove(i)
            }
        }
    }

    fun startNextTrack(guildID: String, noInterrupt: Boolean) {
        val next = songQueue[guildID]!!.firstOrNull()

        if (next != null) {
            player[guildID]!!.startTrack(next.track, noInterrupt)
            songQueue[guildID]!!.removeAt(0)
            //currentChannel?.sendMessage("${next.info.title} by ${next.info.author} has started playing!")?.queue()
        }else{
            player[guildID]!!.stopTrack()
        }
    }
}