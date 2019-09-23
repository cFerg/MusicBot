package io.github.cferg.musicbot.utility

import io.github.cferg.musicbot.extensions.*
import me.aberrantfox.kjdautils.api.dsl.embed
import net.dv8tion.jda.api.entities.*
import java.awt.Color

private val customGreen = Color(0x14B878)
private val customBlue = Color(0x00BFFF)
private val customRed = Color(0xFF4000)

private val defaultImage = listOf(
    "https://i.imgur.com/DzVDu0c.gif",
    "https://i.imgur.com/0W8ZoUs.gif",
    "https://i.imgur.com/EZm2asN.gif"
)

fun currentTrackEmbed(guild: Guild): MessageEmbed {
    val currentSong = guild.fetchCurrentSong() ?: return displayNoSongEmbed()

    return embed {
        color = customBlue
        thumbnail = guild.getMemberById(currentSong.memberID)?.user?.avatarUrl ?: defaultImage.random()

        addField("Now Playing:", formatSong(currentSong, guild))
    }
}

fun displayTrackEmbed(guild: Guild): MessageEmbed {
    val songList = guild.fetchUpcomingSongs()

    if (songList.isEmpty())
        return displayNoSongEmbed()

    return embed {
        color = customBlue
        thumbnail = guild.getMemberById(guild.fetchCurrentSong()!!.memberID)?.user?.avatarUrl ?: defaultImage.random()
        val songSize = songList.size

        songList.forEachIndexed { index, song ->
            when (index) {
                0 -> addField("Now Playing:", formatSong(song, guild))
                1 -> {
                    addField("", "__**Next Songs:**__")
                    addField("", formatSong(song, guild, "$index)"))
                }
                2,3,4 -> addField("", formatSong(song, guild, "$index)"))
                5 -> {
                    addField("", formatSong(song, guild, "$index)"))
                    footer { text = remaining(songSize)}
                }
            }
        }
    }
}

fun addSongEmbed(header: String, description: String, time: Long) = embed {
    addField(header, "$description will start in ~ **${time.toTimeString()}**")
    color = customGreen
}

fun displayNoSongEmbed() = embed {
    addField("There are no more songs currently in the queue. ",
        "If you would like to add a song, use the `Play` or `Search` command.")
    color = customRed
}

private fun remaining(songSize: Int) = when {
    songSize > 7 -> "and ${songSize - 6} others."
    songSize == 7 -> "and 1 other."
    else -> ""
}

private fun formatSong(song: Song, guild: Guild, header: String = "- **Song**:"): String {
    val track = song.track
    val trackInfo = track.info

    return "$header [${trackInfo.title}](${trackInfo.uri})\n" +
        "- **Artist**: ${trackInfo.author}\n" +
        "- **Duration**: ${track.duration.toTimeString()}\n" +
        "- **Queued by**: ${guild.getMemberById(song.memberID)?.asMention}"
}