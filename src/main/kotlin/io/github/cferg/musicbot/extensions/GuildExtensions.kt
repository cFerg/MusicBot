package io.github.cferg.musicbot.extensions

import com.sedmelluq.discord.lavaplayer.player.*
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.*
import io.github.cferg.musicbot.utility.*
import net.dv8tion.jda.api.entities.*
import java.util.ArrayDeque

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

fun Guild.clearByMember(memberID: String) {
    val guildAudio = getGuildAudio()
    guildAudio.songQueue.removeIf {
        it.memberID == memberID
    }
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

            //TODO check if track had played before (make sure time restarts at 0) after a hoist was invoked
            if (!guildAudio.player.startTrack(track, noInterrupt)) {
                print("inner track")
                val currentVC: VoiceChannel? = getMemberById(memberID)?.voiceState?.channel
                    ?: return channel.sendMessage("Please join a voice channel to use this command.").queue()

                audioManager.openAudioConnection(currentVC)
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

fun Guild.nextSong() {
    val guildAudio = getGuildAudio()
    val previousTrack = guildAudio.songQueue.first ?: return
    val textChannelID = previousTrack.channelID
    val textChannel = getTextChannelById(textChannelID) ?: return
    val previousTrackInfo = previousTrack.track.info ?: return

    textChannel.sendMessage("Skipping ${previousTrackInfo.title} by ${previousTrackInfo.author}")

    val songList = guildAudio.songQueue
    songList.removeFirst()

    if (songList.isNotEmpty()) {
        val currentVC = getMemberById(songList.first.memberID)?.voiceState?.channel ?: return nextSong()

        audioManager.openAudioConnection(currentVC)

        guildAudio.player.playTrack(songList.first.track)
    } else {
        guildAudio.player.stopTrack()
        textChannel.sendMessage(displayNoSongEmbed()).queue()
    }
}

fun Guild.setPlayerVolume(volume: Int): Boolean {
    val player = getGuildAudio().player

    player.volume = volume
    return true
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

fun Guild.mutePlayingTrack(): Boolean {
    val guildAudio = getGuildAudio()
    val player = guildAudio.player

    guildAudio.previousVolume = player.volume
    player.volume = 0
    return true
}

fun Guild.unmutePlayingTrack(): Boolean {
    val guildAudio = getGuildAudio()
    val player = guildAudio.player

    player.volume = guildAudio.previousVolume
    return true
}

fun Guild.disconnect(): Boolean {
    val guildAudio = getGuildAudio()

    audioManager.closeAudioConnection()
    guildAudio.player.stopTrack()

    return true
}