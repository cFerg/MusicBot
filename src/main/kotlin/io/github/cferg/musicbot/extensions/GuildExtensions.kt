package io.github.cferg.musicbot.extensions

import com.sedmelluq.discord.lavaplayer.player.*
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.*
import io.github.cferg.musicbot.data.Configuration
import io.github.cferg.musicbot.utility.*
import me.aberrantfox.kjdautils.api.dsl.embed
import me.aberrantfox.kjdautils.extensions.jda.fullName
import net.dv8tion.jda.api.entities.*
import java.awt.Color
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
private fun Guild.getConfig(config: Configuration) = config.guildConfigurations[this.id]!!

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

fun Guild.playSong(config: Configuration, member: Member, channel: TextChannel, songUrl: String, multiSearch: Boolean = true, noInterrupt: Boolean = true) {
    val guildAudio = getGuildAudio()
    val timeRemaining = timeUntilLast()
    val preQueueCount = fetchUpcomingSongs().size
    var isPlaylist = false

    guildAudio.playerManager.loadItem(songUrl, object : AudioLoadResultHandler {
        override fun trackLoaded(track: AudioTrack) {
            if (!assertSongLimit(member, config))
                return errorEmbed(channel,"You have reached the song queue limit of **${songQueueLimit(config)}**"
                , "Attempted song: [${track.info.title}](${track.info.uri})")
            if (!assertSongDuration(track, config))
                return errorEmbed(channel,"That song's duration is **${track.duration.toTimeString()}**, which exceeds the limit of **${songMaxDuration(config).toTimeString()}**"
                , "Attempted song: [${track.info.title}](${track.info.uri})")

            if (noInterrupt) {
                fetchUpcomingSongs().add(Song(track, member.id, channel.id))
            } else {
                fetchUpcomingSongs().addFirst(Song(track, member.id, channel.id))
            }

            if (startTrack(track, noInterrupt)) {
                val currentVC: VoiceChannel? = member.voiceState?.channel
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
                if (!assertSongLimit(member, config))
                    return errorEmbed(channel,"You have reached the song queue limit of **${songQueueLimit(config)}**"
                    , "Attempted playlist: [${playlist.name}]($songUrl)")

                if (!assertPlaylistRole(member, config))
                    return errorEmbed(channel,"You don't have the required role to add a playlist."
                    , "Attempted playlist: [${playlist.name}]($songUrl)")

                val trackSize = playlist.tracks.size
                if (!assertPlaylistLimit(playlist, config))
                    return errorEmbed(channel,"This playlist has **$trackSize** songs, which exceeds the limit of **${playlistQueueLimit(config)}**"
                    , "Attempted playlist: [${playlist.name}]($songUrl)")

                if (!assertPlaylistCount(member, playlist, config)){
                    val remaining = remainingSongLimit(member, config)

                    return errorEmbed(channel,
                        if (remaining <= 0){
                            "Sorry, you have currently reached your song limit. Max is **${songQueueLimit(config)}**"
                        }else{
                            "You can currently add **$remaining** songs - This playlist has **$trackSize**"
                        }, "Attempted playlist: [${playlist.name}]($songUrl)")
                }

                isPlaylist = true

                playlist.tracks.forEachIndexed { _, track ->
                    trackLoaded(track)
                }

                if (preQueueCount > 0) {
                    sendEmbed("[${playlist.name}]($songUrl)", "playlist")
                }
            }else{
                trackLoaded(playlist.tracks.first())
            }
        }

        override fun noMatches() = errorEmbed(channel, "Song Error" , "No matching song found.")

        override fun loadFailed(throwable: FriendlyException) = errorEmbed(channel, "Song Error", "Failed to load song.")

        fun sendEmbed(description: String, header: String){
            channel.sendMessage(addSongEmbed(
                if (preQueueCount == 1){
                    "${member.fullName()} queued a $header to play next."
                }else{
                    "${member.fullName()} queued a $header to start $preQueueCount songs from now."
                }, description, timeRemaining)).queue()
        }

        fun errorEmbed(channel: TextChannel, header: String, body: String) {
            channel.sendMessage(embed { addField(header, body); color = Color(0xFF4000)}).queue()
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

private fun Guild.songMaxDuration(config: Configuration) = getConfig(config).songMaxDuration
private fun Guild.songQueueLimit(config: Configuration) = getConfig(config).songQueueLimit
private fun Guild.playlistQueueLimit(config: Configuration) = getConfig(config).playlistQueueLimit
private fun Guild.remainingSongLimit(member: Member, config: Configuration): Int {
    val songLimit = songQueueLimit(config)

    if (songLimit <= 0){
        return -1
    }

    val currentLimit = fetchUpcomingSongs().count{ song -> song.memberID == member.id }

    return (songLimit - currentLimit)
}
private fun Guild.assertPlaylistCount(member: Member, playlist: AudioPlaylist, config: Configuration): Boolean{
    val songLimit = songQueueLimit(config)

    if (songLimit <= 0){
        return true
    }

    val currentLimit = fetchUpcomingSongs().count{ song -> song.memberID == member.id }
    val playlistCount = playlist.tracks.size

    return (playlistCount < songLimit - currentLimit)
}
private fun Guild.assertSongDuration(track: AudioTrack, config: Configuration): Boolean{
    val durationLimit = songMaxDuration(config)

    if (durationLimit <= 0L){
        return true
    }

    return (track.duration <= durationLimit)
}
private fun Guild.assertSongLimit(member: Member, config: Configuration): Boolean{
    val songLimit = songQueueLimit(config)

    if (songLimit <= 0){
        return true
    }

    val currentLimit = fetchUpcomingSongs().count{ song -> song.memberID == member.id }

    return (currentLimit < songLimit)
}
private fun Guild.assertPlaylistLimit(playlist: AudioPlaylist, config: Configuration): Boolean{
    val playlistLimit = playlistQueueLimit(config)

    if (playlistLimit <= 0){
        return true
    }

    return (playlist.tracks.size <= playlistLimit)
}
private fun Guild.assertPlaylistRole(member: Member, config: Configuration): Boolean{
    val playlistRole = getConfig(config).playlistRole

    if (playlistRole == ""){
        return true
    }

    return member.roles.any { plr -> plr.id == playlistRole}
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

private fun Guild.startTrack(audioTrack: AudioTrack, noInterrupt: Boolean) = getPlayer().startTrack(audioTrack, noInterrupt)
private fun Guild.playTrack(audioTrack: AudioTrack) = getPlayer().playTrack(audioTrack)
private fun Guild.stopTrack() = getPlayer().stopTrack()

fun Guild.restartTrack(): Boolean {
    val track = getPlayer().playingTrack ?: return false

    track.position = 0
    return true
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