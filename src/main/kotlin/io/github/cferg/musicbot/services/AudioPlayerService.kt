package io.github.cferg.musicbot.services

import com.sedmelluq.discord.lavaplayer.player.*
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.*
import io.github.cferg.musicbot.utility.*
import me.aberrantfox.kjdautils.api.annotation.Service
import me.aberrantfox.kjdautils.discord.Discord
import net.dv8tion.jda.api.entities.*
import java.util.ArrayDeque

@Service
class AudioPlayerService(private val discord: Discord, private val embedService: EmbedService) {
    data class Song(val track: AudioTrack, val memberID: String, val channelID: String)
    private data class GuildAudio(val player: AudioPlayer,
                                  val playerManager: DefaultAudioPlayerManager,
                                  val songQueue: ArrayDeque<Song>,
                                  var previousVolume: Int = 30)

    private val guildAudioMap = mutableMapOf<String, GuildAudio>()

    init {
        discord.jda.guilds.forEach { guild ->
            guildAudioMap[guild.id] = guild.toGuildAudio()
        }
    }

    private fun Guild.toGuildAudio(): GuildAudio {
        val playerManager = DefaultAudioPlayerManager()

        AudioSourceManagers.registerLocalSource(playerManager)
        AudioSourceManagers.registerRemoteSources(playerManager)

        val guildAudioPlayer = playerManager.createPlayer()

        guildAudioPlayer.addListener(AudioEventHandler(this@AudioPlayerService, embedService, this))
        audioManager.sendingHandler = AudioPlayerSendHandler(guildAudioPlayer)

        return GuildAudio(guildAudioPlayer, playerManager, ArrayDeque())
    }

    private fun getGuildAudio(guild: Guild) = guildAudioMap.getOrPut(guild.id) { guild.toGuildAudio() }

    fun clearByMember(guildID: String, memberID: String) {
        val guildAudio = guildAudioMap[guildID] ?: return
        guildAudio.songQueue.removeIf {
            it.memberID == memberID
        }
    }

    fun clear(guildID: String) {
        val guild = discord.jda.getGuildById(guildID) ?: return
        val guildAudio = guildAudioMap[guildID] ?: return
        val previousTrack = guildAudio.songQueue.first ?: return
        val textChannelID = previousTrack.channelID
        val songList = guildAudio.songQueue
        val textChannel = guild.getTextChannelById(textChannelID) ?: return

        guildAudio.player.stopTrack()
        songList.clear()

        textChannel.sendMessage(embedService.noSong()).queue()
    }

    fun playSong(guild: Guild, memberID: String, channel: TextChannel, songUrl: String, noInterrupt: Boolean = true) {
        val guildAudio = guildAudioMap[guild.id] ?: return

        guildAudio.playerManager.loadItem(songUrl, object : AudioLoadResultHandler {
            override fun trackLoaded(track: AudioTrack) {
                if (noInterrupt) {
                    guildAudio.songQueue.add(Song(track, memberID, channel.id))
                } else {
                    guildAudio.songQueue.addFirst(Song(track, memberID, channel.id))
                }

                //TODO check if track had played before (make sure time restarts at 0) after a hoist was invoked
                if (!guildAudio.player.startTrack(track, noInterrupt)) {
                    print("inner track")
                    val currentVC: VoiceChannel? = guild.getMemberById(memberID)?.voiceState?.channel
                        ?: return channel.sendMessage("Please join a voice channel to use this command.").queue()

                    guild.audioManager.openAudioConnection(currentVC)
                }
            }

            override fun playlistLoaded(playlist: AudioPlaylist) {
                playlist.tracks.forEachIndexed { _, track ->
                    print("inner playlist")
                    trackLoaded(track)
                }
            }

            override fun noMatches() = channel.sendMessage("No matching song found.").queue()

            override fun loadFailed(throwable: FriendlyException) = channel.sendMessage("Failed to load song.").queue()
        })
    }

    fun nextSong(guildID: String) {
        val guild = discord.jda.getGuildById(guildID) ?: return
        val guildAudio = guildAudioMap[guildID] ?: return
        val previousTrack = guildAudio.songQueue.first ?: return
        val textChannelID = previousTrack.channelID
        val textChannel = guild.getTextChannelById(textChannelID) ?: return
        val previousTrackInfo = previousTrack.track.info ?: return

        textChannel.sendMessage("Skipping ${previousTrackInfo.title} by ${previousTrackInfo.author}")

        val songList = guildAudio.songQueue
        songList.removeFirst()

        if (songList.isNotEmpty()) {
            val currentVC = guild.getMemberById(songList.first.memberID)?.voiceState?.channel
                ?: return nextSong(guildID)

            guild.audioManager.openAudioConnection(currentVC)

            guildAudio.player.playTrack(songList.first.track)
        } else {
            guildAudio.player.stopTrack()
            textChannel.sendMessage(embedService.noSong()).queue()
        }
    }

    fun setPlayerVolume(guild: Guild, volume: Int): Boolean {
        val player = guildAudioMap[guild.id]?.player ?: return false

        player.volume = volume
        return true
    }

    fun fetchNextSong(guild: Guild) = fetchUpcomingSongs(guild).firstOrNull()

    fun fetchUpcomingSongs(guild: Guild) = getGuildAudio(guild).songQueue

    fun isMuted(guild: Guild) = getGuildAudio(guild).player.volume == 0

    fun isTrackPlaying(guild: Guild) = getGuildAudio(guild).player.isPaused.not()

    fun restartTrack(guild: Guild): Boolean {
        val track = getGuildAudio(guild).player.playingTrack ?: return false

        track.position = 0
        return true
    }

    fun mutePlayingTrack(guild: Guild): Boolean {
        val guildAudio = getGuildAudio(guild)
        val player = guildAudio.player

        guildAudio.previousVolume = player.volume
        player.volume = 0
        return true
    }

    fun unmutePlayingTrack(guild: Guild): Boolean {
        val guildAudio = getGuildAudio(guild)
        val player = guildAudio.player

        player.volume = guildAudio.previousVolume
        return true
    }

    fun disconnect(guild: Guild): Boolean {
        val guildAudio = getGuildAudio(guild)

        guild.audioManager.closeAudioConnection()
        guildAudio.player.stopTrack()

        return true
    }
}