package io.github.cferg.musicbot.extensions

import com.sedmelluq.discord.lavaplayer.player.*
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.*
import io.github.cferg.musicbot.utility.*
import me.aberrantfox.kjdautils.extensions.jda.fullName
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
private fun Guild.getPlayer() = getGuildAudio().player

fun Guild.clearByMember(memberID: String): Boolean {
    val preCount = fetchUpcomingSongs().size

    val current = fetchCurrentSong() ?: return false

    val removeFirst = (current.memberID == memberID)

    fetchUpcomingSongs().removeIf {
        it.memberID == memberID
    }

    if (preCount != fetchUpcomingSongs().size){
        if (removeFirst){ nextSong() }

        return true
    }

    if (fetchCurrentSong() == null){
        disconnect()
    }

    return false
}

fun Guild.clear() {
    val currentSong = fetchCurrentSong() ?: return
    val songList = fetchUpcomingSongs()
    val textChannel = getTextChannelById(currentSong.channelID) ?: return

    stopTrack()
    songList.clear()
    disconnect()

    textChannel.sendMessage(displayNoSongEmbed()).queue()
}

fun Guild.playSong(memberID: String, channel: TextChannel, songUrl: String, multiSearch: Boolean = true, noInterrupt: Boolean = true) {
    val guildAudio = getGuildAudio()
    val timeRemaining = timeUntilLast()
    val preQueueCount = fetchUpcomingSongs().size

    var isPlaylist = false

    guildAudio.playerManager.loadItem(songUrl, object : AudioLoadResultHandler {
        override fun trackLoaded(track: AudioTrack) {
            if (noInterrupt) {
                fetchUpcomingSongs().add(Song(track, memberID, channel.id))
            } else {
                fetchUpcomingSongs().addFirst(Song(track, memberID, channel.id))
            }

            if (startTrack(track, noInterrupt)) {
                val currentVC: VoiceChannel? = getMemberById(memberID)?.voiceState?.channel
                    ?: return channel.sendMessage("Please join a voice channel to use this command.").queue()

                audioManager.openAudioConnection(currentVC)
            }else{
                if (!isPlaylist){
                    sendEmbed("[${track.info.title}](${track.info.uri})", "song")
                }
            }
        }

        override fun playlistLoaded(playlist: AudioPlaylist) {
            if (multiSearch){
                isPlaylist = true

                playlist.tracks.forEachIndexed { _, track ->
                    trackLoaded(track)
                }

                sendEmbed("[${playlist.name}]($songUrl)", "playlist")
            }else{
                trackLoaded(playlist.tracks.first())
            }
        }

        override fun noMatches() = channel.sendMessage("No matching song found.").queue()

        override fun loadFailed(throwable: FriendlyException) = channel.sendMessage("Failed to load song.").queue()

        fun sendEmbed(description: String, header: String){
            channel.sendMessage(addSongEmbed(
                if (preQueueCount == 1){
                    "${getMemberById(memberID)!!.fullName()} queued a $header to play next."
                }else{
                    "${getMemberById(memberID)!!.fullName()} queued a $header to start $preQueueCount songs from now."
                }, description, timeRemaining)).queue()
        }
    })
}

fun Guild.nextSong() {
    val currentSong = fetchCurrentSong() ?: return
    val textChannel = getTextChannelById(currentSong.channelID) ?: return
    val songList = fetchUpcomingSongs()

    songList.removeFirst()

    if (songList.isNotEmpty()) {
        val memberID = songList.first.memberID
        val currentVC:VoiceChannel = getMemberById(memberID)?.voiceState?.channel ?: return cleanup(memberID)

        audioManager.openAudioConnection(currentVC)

        playTrack(songList.first.track)
    } else {
        stopTrack()
        textChannel.sendMessage(displayNoSongEmbed()).queue()
        startTimer()
    }
}

private fun Guild.cleanup(memberID: String){
    clearByMember(memberID)
}

fun Guild.setPlayerVolume(volume: Int) {
    getPlayer().volume = volume
}

fun Guild.fetchLastSong() = fetchUpcomingSongs().lastOrNull()
fun Guild.fetchCurrentSong() = fetchUpcomingSongs().firstOrNull()
fun Guild.fetchUpcomingSongs() = getGuildAudio().songQueue

fun Guild.isMuted() = getPlayer().volume == 0
fun Guild.isTrackPlaying() = getPlayer().isPaused.not()

private fun Guild.startTrack(audioTrack: AudioTrack, noInterrupt: Boolean) = getPlayer().startTrack(audioTrack, noInterrupt)
private fun Guild.playTrack(audioTrack: AudioTrack) = getPlayer().playTrack(audioTrack)
private fun Guild.stopTrack() = getPlayer().stopTrack()

fun Guild.restartTrack(): Boolean {
    val track = getPlayer().playingTrack ?: return false

    track.position = 0
    return true
}

fun Guild.mutePlayingTrack() {
    val guildAudio = getGuildAudio()
    val player = getPlayer()

    guildAudio.previousVolume = player.volume
    player.volume = 0
}

fun Guild.unmutePlayingTrack() {
    val guildAudio = getGuildAudio()
    val player = getPlayer()

    player.volume = guildAudio.previousVolume
}

fun Guild.disconnect() {
    audioManager.closeAudioConnection()
    stopTrack()
}

fun Guild.timeUntilLast(): Long {
    val songList = fetchUpcomingSongs().takeIf { it.isNotEmpty() } ?: return 0L
    val currentSongPosition = fetchCurrentSong()!!.track.position

    return songList.sumBy { it.track.duration.toInt() } - currentSongPosition
}

fun Guild.startTimer() {
    var time = 30
    Timer().scheduleAtFixedRate(timerTask {
        if (time > 0){
            if(fetchCurrentSong() != null){
                this.cancel()
            }
            time--
        }else {
            disconnect()
            this.cancel()
        }
    },1000, 10000)
}