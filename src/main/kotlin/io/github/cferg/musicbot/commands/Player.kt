package io.github.cferg.musicbot.commands

import io.github.cferg.musicbot.data.Configuration
import io.github.cferg.musicbot.services.*
import me.aberrantfox.kjdautils.api.dsl.*
import me.aberrantfox.kjdautils.extensions.jda.toMember
import me.aberrantfox.kjdautils.internal.arguments.UrlArg
import net.dv8tion.jda.api.entities.TextChannel

@CommandSet("Player")
fun playerCommands(audioPlayerService: AudioPlayerService, config: Configuration, embedService: EmbedService) = commands {
    command("Play") {
        description = "Play the song listed - If a song is already playing, it's added to a queue."
        requiresGuild = true
        expect(UrlArg)
        execute {
            val url = it.args.component1() as String
            val guild = it.guild!!
            val channel = it.channel as TextChannel
            val member = it.author.toMember(guild)!!

            audioPlayerService.playSong(guild, member.id, channel, url)
        }
    }

    command("Skip") {
        description = "Skips the current song."
        requiresGuild = true
        execute {
            val guild = it.guild!!
            val currentSong = audioPlayerService.fetchNextSong(guild)
                ?: return@execute it.respond("No songs currently queued.")

            val member = it.author.toMember(guild)!!
            val staffRole = config.guildConfigurations[guild.id]?.staffRole
            val queuedSong = it.author.id == currentSong.memberID
            val isStaff = member.roles.any { staff -> staff.id == staffRole }

            if (!queuedSong && !isStaff)
                return@execute it.respond("Sorry, only the person who queued the song or staff can skip.")

            val songInfo = currentSong.track.info

            audioPlayerService.nextSong(guild)
            it.respond("Skipped song: ${songInfo.title} by ${songInfo.author}")
        }
    }

    command("List") {
        description = "Lists the current songs."
        requiresGuild = true
        execute {
            val guild = it.guild!!
            it.respond(embedService.trackDisplay(guild, audioPlayerService))
        }
    }
}