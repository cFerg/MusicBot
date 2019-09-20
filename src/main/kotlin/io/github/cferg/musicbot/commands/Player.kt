package io.github.cferg.musicbot.commands

import io.github.cferg.musicbot.data.Configuration
import io.github.cferg.musicbot.extensions.*
import io.github.cferg.musicbot.utility.displayNoSongEmbed
import io.github.cferg.musicbot.utility.displayTrackEmbed
import me.aberrantfox.kjdautils.api.dsl.*
import me.aberrantfox.kjdautils.extensions.jda.toMember
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

            it.respond("Estimated time until your song starts: ${guild.timeUntilLast().toTimeString()}")
            guild.playSong(member.id, channel, url)
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

            guild.nextSong()

            val previousTrackInfo = currentSong.track.info
            it.respond("Skipping ${previousTrackInfo.title} by ${previousTrackInfo.author}")
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