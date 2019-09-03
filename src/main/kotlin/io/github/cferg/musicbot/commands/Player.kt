package io.github.cferg.musicbot.commands

import io.github.cferg.musicbot.data.Configuration
import io.github.cferg.musicbot.services.AudioPlayerService
import io.github.cferg.musicbot.services.EmbedTrackListService
import me.aberrantfox.kjdautils.api.dsl.CommandSet
import me.aberrantfox.kjdautils.api.dsl.arg
import me.aberrantfox.kjdautils.api.dsl.commands
import me.aberrantfox.kjdautils.extensions.jda.toMember
import me.aberrantfox.kjdautils.internal.arguments.IntegerRangeArg
import me.aberrantfox.kjdautils.internal.arguments.MemberArg
import me.aberrantfox.kjdautils.internal.arguments.UrlArg
import me.aberrantfox.kjdautils.internal.di.PersistenceService
import net.dv8tion.jda.api.entities.TextChannel

@CommandSet("Player")
fun playerCommands(audioPlayerService: AudioPlayerService, config: Configuration, embed: EmbedTrackListService) = commands {
    command("Play") {
        description = "Play the song listed - If a song is already playing, it's added to a queue."
        requiresGuild = true
        expect(arg(UrlArg))
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
            val guildAudio = audioPlayerService.guildAudioMap[it.guild!!.id] ?: return@execute it.respond("Issue running Skip command.")

            if (guildAudio.songQueue.isNullOrEmpty())
                return@execute it.respond("No songs currently queued.")

            val guild = it.guild!!
            val member = it.author.toMember(guild)!!
            val staffRole = config.guildConfigurations[guild.id]?.staffRole

            val playingTrack = guildAudio.player.playingTrack.info
                    ?: return@execute it.respond("No songs currently queued.")

            val queuedSong = it.author.id == guildAudio.songQueue.first.memberID
            val isStaff = member.roles.any { staff -> staff.id == staffRole }

            if (!queuedSong && !isStaff)
                return@execute it.respond("Sorry, only the person who queued the song or staff can skip.")

            audioPlayerService.playSong(guild, it.author.id, it.channel as TextChannel, "Skip") //TODO adjust this, put "skip" to remove error
            it.respond("Skipped song: ${playingTrack.title} by ${playingTrack.author}")
        }
    }

    command("List") {
        description = "Lists the current songs."
        requiresGuild = true
        execute {
            val guild = it.guild!!
            it.respond(embed.trackDisplay(guild, audioPlayerService))
        }
    }
}