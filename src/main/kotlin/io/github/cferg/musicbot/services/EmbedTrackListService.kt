package io.github.cferg.musicbot.services

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import io.github.cferg.musicbot.extensions.toTimeString
import me.aberrantfox.kjdautils.api.annotation.Service
import me.aberrantfox.kjdautils.api.dsl.embed
import net.dv8tion.jda.api.entities.Guild
import java.awt.Color

//TODO move these to a config
val playerOnImage = arrayOf(
    "https://i.imgur.com/DzVDu0c.gif",
    "https://i.imgur.com/0W8ZoUs.gif",
    "https://i.imgur.com/EZm2asN.gif"
)

val playerOffImage = arrayOf(
        "https://i.imgur.com/S7A1t6W.gif",
        "https://i.imgur.com/MuPScTC.gif",
        "https://i.imgur.com/izVWqaM.gif"
)

@Service
class EmbedTrackListService{
    fun trackDisplay(guild: Guild, player: AudioPlayerService) = embed {
        val track = player.currentSong[guild.id]?.track
        val playStatus = player.audio[guild.id]!!.playingTrack

        addField("Now Playing:", if (track != null) {
            "-    **Song**: [${track.info.title}](${track.info.uri})\n" +
            "-    **Artist**: ${track.info.author}\n" +
            "-    **Duration**: ${track.duration.toTimeString()}\n" +
            "-    **Queued by**: ${guild.getMemberById(player.currentSong[guild.id]!!.memberID)?.asMention}"
        } else {
            "No song currently queued."
        })

        color = if (playStatus != null) Color.CYAN else Color.RED
        thumbnail = if (playStatus != null) playerOnImage.random() else playerOffImage.random()

        if (!player.songQueue[guild.id].isNullOrEmpty()){
            addField("", "__**Next Songs:**__")
            player.songQueue[guild.id]!!.forEachIndexed { index, song ->
                addField("",
                        "${index + 1}) [${song.track.info.title}](${song.track.info.uri})\n" +
                        "-    **Artist**: ${song.track.info.author}\n" +
                        "-    **Duration**: ${song.track.duration.toTimeString()}\n" +
                        "-    **Queued by**: ${guild.getMemberById(song.memberID)?.asMention}")
            }
        }
    }

    fun addSong(guild: Guild, memberID: String, track: AudioTrack) = embed{
        addField("**Added a new song:**",
                "-    **Song**: [${track.info.title}](${track.info.uri})\n" +
                "-    **Artist**: ${track.info.author}\n" +
                "-    **Duration**: ${track.duration.toTimeString()}\n" +
                "-    **Queued by**: ${guild.getMemberById(memberID)?.asMention}")
        color = Color.green
    }

    fun noSong() = embed{
        addField("There are no more songs currently in the queue.",
                "If you would like to add a song type:\n" +
                        "\$\$Play <Song URL>")
        color = Color.red
    }
}