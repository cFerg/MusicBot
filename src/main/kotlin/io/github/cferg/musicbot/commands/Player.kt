package io.github.cferg.musicbot.commands

import io.github.cferg.musicbot.data.Configuration
import io.github.cferg.musicbot.extensions.*
import io.github.cferg.musicbot.utility.*
import me.aberrantfox.kjdautils.api.dsl.command.*
import me.aberrantfox.kjdautils.extensions.jda.*
import me.aberrantfox.kjdautils.internal.arguments.*
import net.dv8tion.jda.api.entities.TextChannel

@CommandSet("Player")
fun playerCommands(config: Configuration) = commands {
    command("Play", "Add") {
        description = "Play the song listed - If a song is already playing, it's added to a queue."
        execute(UrlArg("URL")){
            val (url) = it.args
            val guild = it.guild!!
            val channel = it.channel as TextChannel
            val member = it.author.toMember(guild)!!

            guild.playSong(config, member, channel, url)

            if (!it.stealthInvocation){
                it.message.deleteIfExists()
            }
        }
    }

    command("Find", "Search") {
        description = "Lookup a song based on keywords."
        execute(ChoiceArg("YouTube | YT | SoundCloud | SC", "SC", "YT", "SoundCloud", "YouTube"), SentenceArg("Search")){
            val (engine, search) = it.args

            val prefix = when (engine.toLowerCase()){
                "youtube", "yt" -> "ytsearch:"
                "soundcloud", "sc" -> "scsearch:"
                else -> return@execute
            }

            val guild = it.guild!!
            val channel = it.channel as TextChannel
            val member = it.author.toMember(guild)!!

            guild.playSong(config, member, channel, "$prefix$search", multiSearch = false)

            if (!it.stealthInvocation){
                it.message.deleteIfExists()
            }
        }
    }

    command("Skip", "Next") {
        description = "Skips the current song - Attempts to play the next song."
        execute {
            val guild = it.guild!!
            val currentSong = guild.fetchCurrentSong()
                ?: return@execute it.respond(displayNoSongEmbed())

            val member = it.author.toMember(guild)!!
            val staffRole = config.guildConfigurations[guild.id]?.staffRole
            val queuedSong = it.author.id == currentSong.memberID
            val isStaff = member.roles.any { staff -> staff.id == staffRole }

            if (!queuedSong && !isStaff)
                return@execute it.respond("Sorry, only the person who queued the song or staff can skip.")

            val previousTrackInfo = currentSong.track.info
            it.respond("Skipping ${previousTrackInfo.title} by ${previousTrackInfo.author}")
            guild.nextSong()
        }
    }

    command("List") {
        description = "Displays the current songs."
        execute {
            val guild = it.guild!!
            it.respond(displayTrackEmbed(guild))
        }
    }
}