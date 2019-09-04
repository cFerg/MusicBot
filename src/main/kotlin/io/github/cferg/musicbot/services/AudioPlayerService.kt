package io.github.cferg.musicbot.services

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import io.github.cferg.musicbot.utility.AudioPlayerSendHandler
import me.aberrantfox.kjdautils.api.annotation.Service
import me.aberrantfox.kjdautils.discord.Discord
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.VoiceChannel
import java.util.*

@Service
class AudioPlayerService(private val discord: Discord, private val embedService: EmbedService) {
    data class Song(val track: AudioTrack, val memberID: String, val channelID: String)
    data class GuildAudio(var player: AudioPlayer, var playerManager: DefaultAudioPlayerManager,var songQueue: ArrayDeque<Song>)

    var guildAudioMap = mutableMapOf<String, GuildAudio>()
    var trackGuildMap = mutableMapOf<String, String>()

    init {
        discord.jda.guilds.forEach { guild ->
            //TODO test it by not storing player manager
            val playerManager = DefaultAudioPlayerManager()

            AudioSourceManagers.registerLocalSource(playerManager)
            AudioSourceManagers.registerRemoteSources(playerManager)

            val guildAudioPlayer = playerManager.createPlayer()

            guildAudioPlayer.addListener(AudioEventService(discord, this, embedService))
            guild.audioManager.sendingHandler = AudioPlayerSendHandler(guildAudioPlayer)

            guildAudioMap[guild.id] = GuildAudio(guildAudioPlayer, playerManager, ArrayDeque())
        }
    }

    fun clearByMember(guildID: String, memberID: String) {
        val guildAudio = guildAudioMap[guildID] ?: return
        guildAudio.songQueue.removeIf {
            trackGuildMap.remove(it.track.identifier)
            it.memberID == memberID
        }
    }

    fun playSong(guild: Guild, memberID: String, channel: TextChannel, songUrl: String) {
        val guildAudio = guildAudioMap[guild.id] ?: return

        guildAudio.playerManager.loadItem(songUrl, object : AudioLoadResultHandler {
            override fun trackLoaded(track: AudioTrack) {
                guildAudio.songQueue.add(Song(track, memberID, channel.id))
                trackGuildMap[track.identifier] = guild.id

                if (!guildAudio.player.startTrack(track, true)){
                    val currentVC:VoiceChannel? = guild.getMemberById(memberID)?.voiceState?.channel ?:
                    return channel.sendMessage("Please join a voice channel to use this command.").queue()

                    guild.audioManager.openAudioConnection(currentVC)
                }
            }

            override fun playlistLoaded(playlist: AudioPlaylist) {
                playlist.tracks.forEachIndexed { _ , track ->
                    trackLoaded(track)
                }
            }

            override fun noMatches() = channel.sendMessage("No matching song found.").queue()

            override fun loadFailed(throwable: FriendlyException) = channel.sendMessage("Failed to load song.").queue()
        })
    }

    fun nextSong(guildID: String){
        val guild = discord.jda.getGuildById(guildID) ?: return
        val guildAudio = guildAudioMap[guildID] ?: return
        val previousTrack = guildAudio.songQueue.first
        val textChannelID = previousTrack.channelID
        val textChannel = discord.jda.getTextChannelById(textChannelID) ?: return
        val previousTrackInfo = previousTrack.track.info

        textChannel.sendMessage("Skipping ${previousTrackInfo.title} by ${previousTrackInfo.author}")

        val songList = guildAudio.songQueue
        trackGuildMap.remove(songList.first.track.identifier)
        songList.removeFirst()

        if (songList.isNotEmpty()){
            val currentVC = guild.getMemberById(songList.first.memberID)?.voiceState?.channel ?: return nextSong(guildID)

            guild.audioManager.openAudioConnection(currentVC)

            guildAudio.player.playTrack(songList.first.track)
        }else{
            guildAudio.player.stopTrack()
            textChannel.sendMessage(embedService.noSong())
        }
    }
}