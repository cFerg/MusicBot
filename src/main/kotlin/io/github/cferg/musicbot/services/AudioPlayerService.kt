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
class AudioPlayerService(private val discord: Discord, config: Configuration, persistenceService: PersistenceService, private val embeds: EmbedTrackListService) {
    data class Song(val track: AudioTrack, val memberID: String, val guildID: String, val channelSent: String)
    data class GuildAudio(var playerManager: AudioPlayerManager, var player: AudioPlayer, var audioManagers: AudioManager, var audioEventService: AudioEventService)

    var songQueue: MutableMap<String, MutableList<Song>> = mutableMapOf() //guildID and Songs
    var audioQueue: MutableMap<String, GuildAudio> = mutableMapOf() //guildID and GuildAudio
    var guildQueue: MutableMap<String, String> = mutableMapOf() //track identifier and guildID

    var lastChannel: MutableMap<String, String> = mutableMapOf() //guildID and channelID
    var currentSong: MutableMap<String, Song> = mutableMapOf() //guildID and Song
    var currentGuild: MutableMap<String, String> = mutableMapOf() //track identifier and guildID

    init {
        discord.jda.guilds.map { it }.forEach { guild ->
            songQueue[guild.id] = ArrayList()
            audioQueue[guild.id]!!.playerManager = DefaultAudioPlayerManager()

            AudioSourceManagers.registerRemoteSources( audioQueue[guild.id]!!.playerManager)
            AudioSourceManagers.registerLocalSource( audioQueue[guild.id]!!.playerManager)

            audioQueue[guild.id]!!.player =  audioQueue[guild.id]!!.playerManager.createPlayer()
            audioQueue[guild.id]!!.player.volume = 30
            audioQueue[guild.id]!!.audioEventService = AudioEventService(this)
            audioQueue[guild.id]!!.player.addListener( audioQueue[guild.id]!!.audioEventService)

            audioQueue[guild.id]!!.audioManagers = guild.audioManager
            audioQueue[guild.id]!!.audioManagers.sendingHandler = AudioPlayerSendHandler( audioQueue[guild.id]!!.player!!)


            if (config.guildConfigurations[guild.id] == null) {
                config.guildConfigurations[guild.id] = GuildInfo("", "", mutableListOf())
                persistenceService.save(config)
            }
        }
    }

    fun queueAdd(guildID: String, song: Song) {
        if (audioQueue[guildID]!!.player.startTrack(song.track, true)) {
            currentSong[guildID] = song
            currentGuild[song.track.identifier] = guildID
            discord.jda.getTextChannelById(currentSong[guildID]!!.channelSent)?.sendMessage(embeds.trackDisplay(discord.jda.getGuildById(guildID)!!, this))?.queue()
        } else {
            songQueue[guildID]!!.add(song)
            guildQueue[song.track.identifier] = guildID
            discord.jda.getTextChannelById(song.channelSent)?.sendMessage(embeds.addSong(discord.jda.getGuildById(guildID)!!, song.memberID, song.track))?.queue()
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
            println("non null trigger")
            audioQueue[guildID]!!.player.startTrack(next.track, noInterrupt)
            currentSong[guildID] = songQueue[guildID]!!.first()
            currentGuild[next.track.identifier] = guildID
            songQueue[guildID]!!.removeAt(0)
            guildQueue.remove(next.track.identifier)
            lastChannel[guildID] = currentSong[guildID]!!.channelSent

            discord.jda.getTextChannelById(currentSong[guildID]!!.channelSent)!!.sendMessage(embeds.trackDisplay(discord.jda.getGuildById(guildID)!!, this)).queue()
        } else {
            println("null trigger")
            audioQueue[guildID]!!.player.stopTrack()
            discord.jda.getTextChannelById(lastChannel[guildID]!!)!!.sendMessage(embeds.noSong()).queue()
        }
    }
}