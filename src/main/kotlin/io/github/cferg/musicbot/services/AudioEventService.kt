package io.github.cferg.musicbot.services

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import me.aberrantfox.kjdautils.api.annotation.Service
import me.aberrantfox.kjdautils.api.dsl.embed
import me.aberrantfox.kjdautils.discord.Discord
import net.dv8tion.jda.api.entities.TextChannel

@Service
class AudioEventService(private val discord: Discord, private val audioPlayerService: AudioPlayerService, private val embedService: EmbedService) : AudioEventAdapter() {
    override fun onPlayerPause(player: AudioPlayer) = Unit
    override fun onPlayerResume(player: AudioPlayer) = Unit
    override fun onTrackStart(player: AudioPlayer, track: AudioTrack) {
        val guildID = audioPlayerService.trackGuildMap[track.identifier]!!
        val guild = discord.jda.getGuildById(guildID)!!
        val guildAudio = audioPlayerService.guildAudioMap[guildID]!!
        val firstSong = guildAudio.songQueue.first
        val textChannel: TextChannel = guild.getTextChannelById(firstSong.channelID)!!

        textChannel.sendMessage(embedService.trackDisplay(guild, audioPlayerService)).queue()
    }

    override fun onTrackEnd(player: AudioPlayer, track: AudioTrack, endReason: AudioTrackEndReason) {
        if (endReason.mayStartNext || endReason == AudioTrackEndReason.FINISHED) {
            val guildID = audioPlayerService.trackGuildMap[track.identifier]!!

            audioPlayerService.nextSong(guildID)
        }
    }

    override fun onTrackException(player: AudioPlayer, track: AudioTrack, exception: FriendlyException) {
        val guildID = audioPlayerService.trackGuildMap[track.identifier]!!

        audioPlayerService.nextSong(guildID)
    }

    override fun onTrackStuck(player: AudioPlayer, track: AudioTrack, thresholdMs: Long) {
        val guildID = audioPlayerService.trackGuildMap[track.identifier]!!

        audioPlayerService.nextSong(guildID)
    }
}