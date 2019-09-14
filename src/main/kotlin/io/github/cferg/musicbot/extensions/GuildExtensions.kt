package io.github.cferg.musicbot.extensions

import com.sedmelluq.discord.lavaplayer.player.*
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.*
import io.github.cferg.musicbot.utility.*
import net.dv8tion.jda.api.entities.*
import java.util.*
import kotlin.concurrent.timerTask

data class Song(val track: AudioTrack, val memberID: String, val channelID: String)

private data class GuildAudio(val player: AudioPlayer,
                              val playerManager: DefaultAudioPlayerManager,
                              val songQueue: ArrayDeque<Song>,
                              var previousVolume: Int = 30)

private val guildAudioMap = mutableMapOf<String, GuildAudio>()

private fun Guild.toGuildAudio(): GuildAudio {
    val playerManager = DefaultAudioPlayerManager()

    AudioSourceManagers.registerLocalSource(playerManager)
    AudioSourceManagers.registerRemoteSources(playerManager)

    val guildAudioPlayer = playerManager.createPlayer()

    guildAudioPlayer.addListener(AudioEventHandler(this))
    audioManager.sendingHandler = AudioPlayerSendHandler(guildAudioPlayer)

    return GuildAudio(guildAudioPlayer, playerManager, ArrayDeque())
}

private fun Guild.getGuildAudio() = guildAudioMap.getOrPut(id) { toGuildAudio() }

fun Guild.clearByMember(memberID: String): Boolean {
    val guildAudio = getGuildAudio()

    val preCount = guildAudio.songQueue.size

    guildAudio.songQueue.removeIf {
        it.memberID == memberID
    }

    if (preCount != guildAudio.songQueue.size){
        nextSong(false)
        return true
    }

    return false
}

fun Guild.clear() {
    val guildAudio = getGuildAudio()
    val nextSong = fetchNextSong() ?: return
    val textChannelID = nextSong.channelID
    val songList = guildAudio.songQueue
    val textChannel = getTextChannelById(textChannelID) ?: return

    guildAudio.player.stopTrack()
    songList.clear()

    textChannel.sendMessage(displayNoSongEmbed()).queue()
}

fun Guild.playSong(memberID: String, channel: TextChannel, songUrl: String, noInterrupt: Boolean = true) {
    val guildAudio = getGuildAudio()

    guildAudio.playerManager.loadItem(songUrl, object : AudioLoadResultHandler {
        override fun trackLoaded(track: AudioTrack) {
            if (noInterrupt) {
                guildAudio.songQueue.add(Song(track, memberID, channel.id))
            } else {
                guildAudio.songQueue.addFirst(Song(track, memberID, channel.id))
            }

            if (guildAudio.player.startTrack(track, noInterrupt)) {
                val currentVC: VoiceChannel? = getMemberById(memberID)?.voiceState?.channel
                    ?: return channel.sendMessage("Please join a voice channel to use this command.").queue()

                audioManager.openAudioConnection(currentVC)
            }
        }

        override fun playlistLoaded(playlist: AudioPlaylist) {
            playlist.tracks.forEachIndexed { _, track ->
                trackLoaded(track)
            }
        }

        override fun noMatches() = channel.sendMessage("No matching song found.").queue()

        override fun loadFailed(throwable: FriendlyException) = channel.sendMessage("Failed to load song.").queue()
    })
}

fun Guild.nextSong(safe:Boolean = true) {
    val guildAudio = getGuildAudio()
    val previousTrack = guildAudio.songQueue.first ?: return
    val textChannelID = previousTrack.channelID
    val textChannel = getTextChannelById(textChannelID) ?: return
    val previousTrackInfo = previousTrack.track.info ?: return

    val songList = guildAudio.songQueue

    if(safe) {
        textChannel.sendMessage("Skipping ${previousTrackInfo.title} by ${previousTrackInfo.author}").queue()
        songList.removeFirst()
    }

    if (songList.isNotEmpty()) {
        val currentVC = getMemberById(songList.first.memberID)?.voiceState?.channel ?: return nextSong()

        audioManager.openAudioConnection(currentVC)

        guildAudio.player.playTrack(songList.first.track)
    } else {
        guildAudio.player.stopTrack()
        textChannel.sendMessage(displayNoSongEmbed()).queue()
        startTimer()
    }
}

fun Guild.setPlayerVolume(volume: Int) {
    val player = getGuildAudio().player

    player.volume = volume
}

fun Guild.fetchNextSong() = fetchUpcomingSongs().firstOrNull()

fun Guild.fetchUpcomingSongs() = getGuildAudio().songQueue

fun Guild.isMuted() = getGuildAudio().player.volume == 0

fun Guild.isTrackPlaying() = getGuildAudio().player.isPaused.not()

fun Guild.restartTrack(): Boolean {
    val track = getGuildAudio().player.playingTrack ?: return false

    track.position = 0
    return true
}

fun Guild.mutePlayingTrack() {
    val guildAudio = getGuildAudio()
    val player = guildAudio.player

    guildAudio.previousVolume = player.volume
    player.volume = 0
}

fun Guild.unmutePlayingTrack() {
    val guildAudio = getGuildAudio()
    val player = guildAudio.player

    player.volume = guildAudio.previousVolume
}

fun Guild.disconnect() {
    val guildAudio = getGuildAudio()

    audioManager.closeAudioConnection()
    guildAudio.player.stopTrack()
}

fun Guild.startTimer() {
    var time = 30
    Timer().scheduleAtFixedRate(timerTask {
        if (time > 0){
            if(fetchNextSong() != null){
                this.cancel()
            }
            time--
        }else {
            disconnect()
            this.cancel()
        }
    },1000, 10000)
}