package io.github.cferg.musicbot.utility

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.*
import io.github.cferg.musicbot.services.*
import net.dv8tion.jda.api.entities.Guild

class AudioEventHandler(private val audioPlayerService: AudioPlayerService,
                        private val embedService: EmbedService,
                        private val guild: Guild) : AudioEventAdapter() {
    override fun onPlayerPause(player: AudioPlayer) = Unit
    override fun onPlayerResume(player: AudioPlayer) = Unit
    override fun onTrackStart(player: AudioPlayer, track: AudioTrack) {
        val firstSong = audioPlayerService.fetchNextSong(guild) ?: return
        val textChannel = guild.getTextChannelById(firstSong.channelID)!!

        textChannel.sendMessage(embedService.trackDisplay(guild, audioPlayerService)).queue()
    }

    override fun onTrackEnd(player: AudioPlayer, track: AudioTrack, endReason: AudioTrackEndReason) {
        if (endReason.mayStartNext || endReason == AudioTrackEndReason.FINISHED)
            audioPlayerService.nextSong(guild.id)
    }

    override fun onTrackException(player: AudioPlayer, track: AudioTrack, exception: FriendlyException) {
        audioPlayerService.nextSong(guild.id)
    }

    override fun onTrackStuck(player: AudioPlayer, track: AudioTrack, thresholdMs: Long) {
        audioPlayerService.nextSong(guild.id)
    }
}