package io.github.cferg.musicbot.services

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import io.github.cferg.musicbot.data.Configuration
import io.github.cferg.musicbot.data.GuildInfo
import io.github.cferg.musicbot.utility.AudioPlayerSendHandler
import me.aberrantfox.kjdautils.api.annotation.Service
import me.aberrantfox.kjdautils.discord.Discord
import me.aberrantfox.kjdautils.internal.di.PersistenceService
import net.dv8tion.jda.api.managers.AudioManager

@Service
class AudioPlayerService(discord: Discord, config: Configuration, persistenceService: PersistenceService, embeds: EmbedTrackListService) {
    var songQueue: MutableMap<String, MutableList<Song>> = mutableMapOf()

    data class Song(val track: AudioTrack, val memberID: String, val guildID: String, val channelSent: String)

    var currentSong: MutableMap<String, Song> = mutableMapOf()
    var currentGuild: MutableMap<String, String> = mutableMapOf()
    var guildQueue: MutableMap<String, String> = mutableMapOf()
    var playerManager: MutableMap<String, AudioPlayerManager> = mutableMapOf()
    var player: MutableMap<String, AudioPlayer> = mutableMapOf()
    var audioManagers: MutableMap<String, AudioManager> = mutableMapOf()
    var audioEventService: MutableMap<String, AudioEventService> = mutableMapOf()

    init {
        discord.jda.guilds.map { it }.forEach { guild ->
            songQueue[guild.id] = ArrayList()
            playerManager[guild.id] = DefaultAudioPlayerManager()

            AudioSourceManagers.registerRemoteSources(playerManager[guild.id])
            AudioSourceManagers.registerLocalSource(playerManager[guild.id])

            player[guild.id] = playerManager[guild.id]!!.createPlayer()
            player[guild.id]!!.volume = 30
            audioEventService[guild.id] = AudioEventService(this, embeds, discord)
            player[guild.id]!!.addListener(audioEventService[guild.id])

            audioManagers[guild.id] = guild.audioManager
            audioManagers[guild.id]!!.sendingHandler = AudioPlayerSendHandler(player[guild.id]!!)


            if (config.guildConfigurations[guild.id] == null) {
                config.guildConfigurations[guild.id] = GuildInfo("", "", mutableListOf())
                persistenceService.save(config)
            }
        }
    }

    fun queueAdd(guildID: String, song: Song) {
        if (player[guildID]!!.startTrack(song.track, true)) {
            currentSong[guildID] = song
            currentGuild[song.track.identifier] = guildID
        } else {
            songQueue[guildID]!!.add(song)
            guildQueue[song.track.identifier] = guildID
        }
    }

    fun clearByMember(guildID: String, memberID: String) {
        for (i in songQueue[guildID]!!) {
            if (i.memberID == memberID) {
                songQueue[guildID]!!.remove(i)
                guildQueue.remove(i.track.identifier)
            }
        }

        if (currentSong[guildID]?.memberID == memberID) {
            startNextTrack(guildID, true)
        }
    }

    fun startNextTrack(guildID: String, noInterrupt: Boolean) {
        val next = songQueue[guildID]!!.firstOrNull()

        if (next != null) {
            player[guildID]!!.startTrack(next.track, noInterrupt)
            currentSong[guildID] = songQueue[guildID]!!.first()
            currentGuild[next.track.identifier] = guildID
            songQueue[guildID]!!.removeAt(0)
            guildQueue.remove(next.track.identifier)
            //currentChannel?.sendMessage("${next.info.title} by ${next.info.author} has started playing!")?.queue()
        } else {
            player[guildID]!!.stopTrack()
        }
    }
}