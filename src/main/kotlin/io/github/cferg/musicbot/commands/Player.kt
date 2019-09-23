package io.github.cferg.musicbot.commands

import io.github.cferg.musicbot.data.Configuration
import io.github.cferg.musicbot.extensions.*
import io.github.cferg.musicbot.utility.displayNoSongEmbed
import io.github.cferg.musicbot.utility.displayTrackEmbed
import me.aberrantfox.kjdautils.api.dsl.*
import me.aberrantfox.kjdautils.extensions.jda.deleteIfExists
import me.aberrantfox.kjdautils.extensions.jda.toMember
import me.aberrantfox.kjdautils.internal.arguments.ChoiceArg
import me.aberrantfox.kjdautils.internal.arguments.SentenceArg
import me.aberrantfox.kjdautils.internal.arguments.UrlArg
import net.dv8tion.jda.api.entities.TextChannel

@CommandSet("Player")
fun playerCommands(config: Configuration) = commands {
    command("Play") {
        description = "Play the song listed - If a song is already playing, it's added to a queue."
        requiresGuild = true
        expect(UrlArg)
        execute {
            val url = it.args.component1() as String
            val guild = it.guild!!
            val channel = it.channel as TextChannel
            val member = it.author.toMember(guild)!!

            guild.playSong(config, member, channel, url)

            if (!it.stealthInvocation){
                it.message.deleteIfExists()
            }
        }
    }

    command("Search") {
        description = "Search a song based on keywords."
        requiresGuild = true
        expect(ChoiceArg("YouTube | YT | SoundCloud | SC", "SC", "YT", "SoundCloud", "YouTube"), SentenceArg)
        execute {
            val engine = it.args.component1() as String
            val search = it.args.component2() as String

            val prefix = when (engine.toLowerCase()){
                "youtube", "yt" -> {
                    "ytsearch:"
                }
                "soundcloud", "sc" -> {
                    "scsearch:"
                }
                else -> {
                    return@execute
                }
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

    command("Skip") {
        description = "Skips the current song."
        requiresGuild = true
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
        description = "Lists the current songs."
        requiresGuild = true
        execute {
            val guild = it.guild!!
            it.respond(displayTrackEmbed(guild))
        }
    }
}