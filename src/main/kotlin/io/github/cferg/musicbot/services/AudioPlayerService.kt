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
class AudioPlayerService(discord: Discord) {
    data class Song(val track: AudioTrack, val memberID: String)
    data class GuildAudio(var player: AudioPlayer, var songQueue: ArrayDeque<Song>)

    private var guildAudioMap = mutableMapOf<String, GuildAudio>()

    init {
        discord.jda.guilds.forEach { guild ->
            val guildAudioPlayer = DefaultAudioPlayerManager().createPlayer()

            guildAudioPlayer.addListener(AudioEventService(this))
            guild.audioManager.sendingHandler = AudioPlayerSendHandler(guildAudioPlayer)

            guildAudioMap[guild.id] = GuildAudio(guildAudioPlayer, ArrayDeque())
        }
    }

    fun clearByMember(guildID: String, memberID: String) {
        guildAudioMap[guildID]?.songQueue?.removeIf { it.memberID == memberID }
    }

    fun queueSong()

    fun playNext()

    private fun playSong(guild: Guild, memberId: String, channel: TextChannel, songUrl: String) {
        val guildAudio = guildAudioMap[guild.id] ?: return

        if (songUrl.isEmpty()) return

        DefaultAudioPlayerManager().loadItem(songUrl, object : AudioLoadResultHandler {
            override fun loadFailed(throwable: FriendlyException) = channel.sendMessage("Failed to load song.").queue()
            override fun noMatches() = channel.sendMessage("No matching song found.").queue()

            override fun trackLoaded(track: AudioTrack) {
                if (guildAudio.songQueue.isEmpty() && guildAudio.player.isPaused)
                    guildAudio.player.playTrack(track)
                else
                    guildAudio.songQueue.add(Song(track, memberId))
            }

            override fun playlistLoaded(playlist: AudioPlaylist) {
                playlist.tracks.forEachIndexed { index, track ->
                    if (guildAudio.songQueue.isEmpty() && index == 0)
                        guildAudio.player.playTrack(track)
                    else
                        guildAudio.songQueue.add(Song(track, memberId))
                }
            }
        })
    }
}