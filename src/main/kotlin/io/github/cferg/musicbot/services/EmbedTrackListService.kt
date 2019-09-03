package io.github.cferg.musicbot.services

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import io.github.cferg.musicbot.extensions.toTimeString
import me.aberrantfox.kjdautils.api.annotation.Service
import me.aberrantfox.kjdautils.api.dsl.embed
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.MessageEmbed
import java.awt.Color
import java.util.*

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
class EmbedTrackListService {
    fun trackDisplay(guild: Guild, player: AudioPlayerService): MessageEmbed {
        val track: AudioTrack = player.guildAudioMap[guild.id]?.songQueue?.firstOrNull()?.track ?: return noSong()
        val playStatus = player.guildAudioMap[guild.id]?.player?.playingTrack ?: false

        return embed {
            val audio = player.guildAudioMap[guild.id] ?:  return@embed
            val songList = audio.songQueue

            addField("Now Playing:",
                "- **Song**: [${track.info.title}](${track.info.uri})\n" +
                "- **Artist**: ${track.info.author}\n" +
                "- **Duration**: ${track.duration.toTimeString()}\n" +
                "- **Queued by**: ${guild.getMemberById(songList.first.memberID)?.asMention}")

            color = if (playStatus == true) Color.CYAN else Color.RED
            thumbnail = if (playStatus == true) playerOnImage.random() else playerOffImage.random()

            //TODO if there's a better way to just start at index 1 with songList, do that
            if (songList.size > 1) {
                val nextList = ArrayDeque<AudioPlayerService.Song>(songList)
                nextList.removeFirst()

                addField("", "__**Next Songs:**__")
                nextList.forEachIndexed{ index , song ->
                    addField("",
                        "${index + 1}) [${song.track.info.title}](${song.track.info.uri})\n" +
                        "- **Artist**: ${song.track.info.author}\n" +
                        "- **Duration**: ${song.track.duration.toTimeString()}\n" +
                        "- **Queued by**: ${guild.getMemberById(song.memberID)?.asMention}")
                }
            }
        }
    }

    fun addSong(guild: Guild, memberID: String, track: AudioTrack) = embed {
        addField("**Added a new song:**",
                "- **Song**: [${track.info.title}](${track.info.uri})\n" +
                "- **Artist**: ${track.info.author}\n" +
                "- **Duration**: ${track.duration.toTimeString()}\n" +
                "- **Queued by**: ${guild.getMemberById(memberID)?.asMention}")
        color = Color.green
    }

    fun noSong() = embed {
        addField("There are no more songs currently in the queue.",
                "If you would like to add a song type:\n" +
                        "\$\$Play <Song URL>")
        color = Color.red
    }
}