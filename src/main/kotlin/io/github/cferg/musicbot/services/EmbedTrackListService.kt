package io.github.cferg.musicbot.services

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
    fun updateDisplay(guild: Guild, player: AudioPlayerService) = embed {
        color = Color.CYAN
        val track = player.currentSong[guild.id]?.track!!.info
        val display = if (track != null) {
            "${track.title} by ${track.author}"
        } else {
            "No song currently queued."
        }

        thumbnail = playerOnImage.random() //TODO replace with is playing check to specify url

        addInlineField("Current Song: ", display)
        addBlankField(true)

        if (player.songQueue[guild.id] != null){
            player.songQueue[guild.id]!!.forEachIndexed { index, song ->
                addField("${index + 1}) ${song.track.info.title}",
                        "     Artist: ${song.track.info.author}\n" +
                        "     Duration: ${song.track.duration.toTimeString()}\n" +
                        "     Queued by: ${guild.getMemberById(song.memberID)?.asMention}")
            }
        }
    }
}