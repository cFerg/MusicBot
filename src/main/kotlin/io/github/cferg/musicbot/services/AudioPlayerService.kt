package io.github.cferg.musicbot.services

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import io.github.cferg.musicbot.data.Configuration
import io.github.cferg.musicbot.data.GuildInfo
import me.aberrantfox.kjdautils.api.annotation.Service
import me.aberrantfox.kjdautils.discord.Discord
import me.aberrantfox.kjdautils.internal.di.PersistenceService

@Service
class AudioPlayerService(discord: Discord, config: Configuration, persistenceService: PersistenceService) {
    var songQueue: MutableMap<String, MutableList<Song>> = mutableMapOf()
    data class Song(val track: AudioTrack, val memberID: String)
    var currentSong : MutableMap<String, Song> = mutableMapOf()
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

            if (config.guildConfigurations[id] == null) {
                config.guildConfigurations[id] = GuildInfo("", "", mutableListOf())
                persistenceService.save(config)
            }
        }
    }

    fun queueAdd(guildID: String, song: Song) {
        if (player[guildID]!!.playingTrack == null){
            player[guildID]!!.startTrack(song.track, true)
            currentSong[guildID] = song
        }else{
            songQueue[guildID]!!.add(song)
        }
    }

    fun clearByMember(guildID: String, memberID: String) {
        for (i in songQueue[guildID]!!) {
            if (i.memberID == memberID) {
                songQueue[guildID]!!.remove(i)
            }
        }

        if (currentSong[guildID]?.memberID == memberID){
            startNextTrack(guildID, true)
        }
    }

    fun startNextTrack(guildID: String, noInterrupt: Boolean) {
        val next = songQueue[guildID]!!.firstOrNull()

        if (next != null) {
            player[guildID]!!.startTrack(next.track, noInterrupt)
            currentSong[guildID] = songQueue[guildID]!!.first()
            songQueue[guildID]!!.removeAt(0)
            //currentChannel?.sendMessage("${next.info.title} by ${next.info.author} has started playing!")?.queue()
        } else {
            player[guildID]!!.stopTrack()
        }
    }
}