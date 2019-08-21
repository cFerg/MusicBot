package commands

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import data.Channels
import me.aberrantfox.kjdautils.api.dsl.CommandSet
import me.aberrantfox.kjdautils.api.dsl.arg
import me.aberrantfox.kjdautils.api.dsl.commands
import me.aberrantfox.kjdautils.internal.arguments.IntegerRangeArg
import me.aberrantfox.kjdautils.internal.arguments.UrlArg
import net.dv8tion.jda.api.managers.AudioManager
import utility.AudioPlayerSendHandler
import services.AudioPlayerService

@CommandSet("Player")
fun playerCommands(plugin: AudioPlayerService, channels: Channels) = commands {
    command("Play") {
        description = "Play the song listed - If a song is already playing, it's added to a queue."
        requiresGuild = true
        expect(arg(UrlArg))
        execute {
            val url = it.args.component1() as String
            val guild = it.guild!!
            val am: AudioManager = guild.audioManager

            am.sendingHandler = AudioPlayerSendHandler(plugin.player)
            am.openAudioConnection(guild.getVoiceChannelById(channels.getVoiceChannel(it.guild!!.id, it.channel.id)))

            plugin.playerManager.loadItem(url, object : AudioLoadResultHandler {
                override fun trackLoaded(track: AudioTrack) {
                    plugin.queue(track)
                    it.respond("Added song: ${track.info.title} by ${track.info.author}")
                }

                override fun playlistLoaded(playlist: AudioPlaylist) {
                    for (track in playlist.tracks) {
                        plugin.queue(track)
                        it.respond("Added song: ${track.info.title} by ${track.info.author}")
                    }
                }

                override fun noMatches() = it.respond("No matching song found")

                override fun loadFailed(throwable: FriendlyException) = it.respond("Error, could not load track.")
            })
        }
    }

    command("Pause") {
        description = "Pauses the current song."
        requiresGuild = true
        execute {
            plugin.player.isPaused = true
            //TODO fix long to seconds display later
            val duration: Double = (plugin.player.playingTrack.position / 100).toDouble()
            it.respond("Paused song: ${plugin.player.playingTrack.info.title} at ${duration / 10} seconds.")
        }
    }

    command("Resume") {
        description = "Continues the last song (If one is still queued)"
        requiresGuild = true
        execute {
            plugin.player.isPaused = false
            //TODO fix long to seconds display later
            val duration: Double = (plugin.player.playingTrack.position / 100).toDouble()
            it.respond("Resumed song: ${plugin.player.playingTrack.info.title} from ${duration / 10} seconds.")
        }
    }

    //TODO add next as an alias
    command("Skip") {
        description = "Skips the current song."
        requiresGuild = true
        execute {
            if (plugin.queue.isNullOrEmpty()) {
                it.respond("No songs currently queued.")
            } else {
                it.respond("Skipped song: ${plugin.player.playingTrack.info.title} by ${plugin.player.playingTrack.info.author}")
                plugin.startNextTrack(false)
            }
        }
    }

    command("Restart") {
        description = "Replays the current song from the beginning."
        requiresGuild = true
        execute {
            plugin.player.playingTrack.position = 0
            it.respond("Restarted the song: ${plugin.player.playingTrack.info.title}")
        }
    }

    command("Clear") {
        description = "Removes all currently queued songs."
        requiresGuild = true
        execute {
            plugin.queue.clear()
            it.respond("Cleared the current list of songs.")
        }
    }

    command("Volume") {
        description = "Adjust volume from range 0-100"
        requiresGuild = true
        expect(IntegerRangeArg(min = 0, max = 100))
        execute {
            plugin.player.volume = it.args.component1() as Int
        }
    }

    var previousVolume: Int = plugin.player.volume

    command("Mute") {
        description = "Mute bot, but keeps it playing."
        requiresGuild = true
        execute {
            previousVolume = plugin.player.volume
            plugin.player.volume = 0
            it.respond("The bot is now muted.")
        }
    }

    command("Unmute") {
        description = "Sets bot's volume back to previous level before it was muted."
        requiresGuild = true
        execute {
            plugin.player.volume = previousVolume
            it.respond("The bot is now unmuted.")
        }
    }
}