package io.github.cferg.musicbot.services

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import io.github.cferg.musicbot.utility.AudioPlayerSendHandler
import me.aberrantfox.kjdautils.api.annotation.Service
import me.aberrantfox.kjdautils.discord.Discord
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.TextChannel
import java.util.*

@Service
class AudioPlayerService(private val discord: Discord) {
    data class Song(val track: AudioTrack, val memberID: String, val channelID: String)
    data class GuildAudio(var player: AudioPlayer, var songQueue: ArrayDeque<Song>)

    var guildAudioMap = mutableMapOf<String, GuildAudio>()
    var trackGuildMap = mutableMapOf<String, String>()

    init {
        discord.jda.guilds.forEach { guild ->
            val guildAudioPlayer = DefaultAudioPlayerManager().createPlayer()

            guildAudioPlayer.addListener(AudioEventService(this))
            guild.audioManager.sendingHandler = AudioPlayerSendHandler(guildAudioPlayer)

            guildAudioMap[guild.id] = GuildAudio(guildAudioPlayer, ArrayDeque())
        }
    }

    fun clearByMember(guildID: String, memberID: String) {
        val guildAudio = guildAudioMap[guildID] ?: return
        guildAudio.songQueue.removeIf {
            trackGuildMap.remove(it.track.identifier)
            it.memberID == memberID
        }
    }

    fun playSong(guild: Guild, memberId: String, channel: TextChannel, songUrl: String) {
        val guildAudio = guildAudioMap[guild.id] ?: return

        if (songUrl.isEmpty()) return

        DefaultAudioPlayerManager().loadItem(songUrl, object : AudioLoadResultHandler {
            override fun loadFailed(throwable: FriendlyException) = channel.sendMessage("Failed to load song.").queue()
            override fun noMatches() = channel.sendMessage("No matching song found.").queue()

            override fun trackLoaded(track: AudioTrack) {
                if (guildAudio.songQueue.isEmpty() && guildAudio.player.isPaused)
                    guildAudio.player.playTrack(track)
                else
                    guildAudio.songQueue.add(Song(track, memberId, channel.id))
            }

            override fun playlistLoaded(playlist: AudioPlaylist) {
                playlist.tracks.forEachIndexed { index, track ->
                    if (guildAudio.songQueue.isEmpty() && index == 0)
                        guildAudio.player.playTrack(track)
                    else
                        guildAudio.songQueue.add(Song(track, memberId, channel.id))
                }
            }
        })
    }

    fun nextSong(guildID: String){
        val guildAudio = guildAudioMap[guildID] ?: return
        val previousTrack = guildAudio.songQueue.first
        val textChannelID = previousTrack.channelID
        val textChannel = discord.jda.getTextChannelById(textChannelID) ?: return
        val previousTrackInfo = previousTrack.track.info

        textChannel.sendMessage("Skipping ${previousTrackInfo.title} by ${previousTrackInfo.author}")

        val songList = guildAudio.songQueue
        songList.removeFirst()

        if (songList.isNotEmpty()){
            guildAudio.player.startTrack(songList.first.track, false)
        }else{
            guildAudio.player.stopTrack()
        }
    }
}