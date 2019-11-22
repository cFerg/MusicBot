package io.github.cferg.musicbot.utility

import com.google.gson.Gson
import io.github.cferg.musicbot.botPrefix
import io.github.cferg.musicbot.extensions.*
import me.aberrantfox.kjdautils.api.dsl.embed
import me.aberrantfox.kjdautils.extensions.jda.fullName
import net.dv8tion.jda.api.entities.*
import java.awt.Color

private val customGreen = Color(0x14B878)
private val customBlue = Color(0x00BFFF)
private val customRed = Color(0xFF4000)

fun currentTrackEmbed(guild: Guild): MessageEmbed {
    val currentSong = guild.fetchCurrentSong() ?: return displayNoSongEmbed()

    return embed {
        color = customBlue
        thumbnail = currentSong.track.info.artworkUrl

        addField("Now Playing:", formatSong(currentSong, guild))
    }
}

fun displayTrackEmbed(guild: Guild): MessageEmbed {
    val songList = guild.fetchUpcomingSongs()

    if (songList.isEmpty())
        return displayNoSongEmbed()

    return embed {
        color = customBlue
        thumbnail = songList.first.track.info.artworkUrl
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

private data class Properties(val version: String, val author: String, val repository: String)

private val propFile = Properties::class.java.getResource("/properties.json").readText()
private val project = Gson().fromJson(propFile, Properties::class.java)

private val version = project.version
private val author = project.author
val source = project.repository

fun botInfo(guild: Guild) = embed {
    val self = guild.jda.selfUser

    color = customGreen
    thumbnail = self.effectiveAvatarUrl
    addField(self.fullName(), "A multi-guild music bot for discord.")
    addInlineField("Version", version)
    addInlineField("Prefix", botPrefix)
    addInlineField("Author", "[${author}](https://discordapp.com/users/167417801873555456/)")
    addInlineField("Contributors",
            "[JakeyWakey#1569](https://discordapp.com/users/254786431656919051/)\n" +
                    "[Elliott#0001](https://discordapp.com/users/335628039302021121/)")
    addField("Source", "[$source]($source)")
}

fun configurationNeeded(guild: Guild, vararg lines: String) = embed {
    color = customRed

    var paragraph = ""

    lines.forEach { line ->
        paragraph += "\n$line"
    }

    addField("Configuration needed for: **__${guild.name}__**", paragraph)
}

fun configurationSuccessful(vararg lines: String) = embed {
    color = customGreen

    var paragraph = ""

    lines.forEach { line ->
        paragraph += "\n$line"
    }

    addField("Thank you - The Server is now setup!", paragraph)
}