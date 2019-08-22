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
    //TODO add add as an alias of an argumented command
    command("Play") {
        description = "Play the song listed - If a song is already playing, it's added to a queue."
        requiresGuild = true
        expect(arg(UrlArg))
        execute {
            val url = it.args.component1() as String
            val guild = it.guild!!
            val am: AudioManager = guild.audioManager

            am.sendingHandler = AudioPlayerSendHandler(plugin.player[guild.id]!!)
            am.openAudioConnection(guild.getVoiceChannelById(channels.getVoiceChannel(guild.id, it.channel.id)))

            plugin.playerManager[guild.id]!!.loadItem(url, object : AudioLoadResultHandler {
                override fun trackLoaded(track: AudioTrack) {
                    plugin.queue(guild.id, track)
                    it.respond("Added song: ${track.info.title} by ${track.info.author}")
                }

                override fun playlistLoaded(playlist: AudioPlaylist) {
                    //TODO add permission check for individuals to play playlists
                    for (track in playlist.tracks) {
                        plugin.queue(guild.id, track)
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
            if (plugin.player[it.guild!!.id]!!.isPaused){
                it.respond("Player is already paused.")
            }else {
                plugin.player[it.guild!!.id]!!.isPaused = true
                //TODO fix long to seconds display later
                val duration: Double = (plugin.player[it.guild!!.id]!!.playingTrack.position / 100).toDouble()
                it.respond("Paused song: ${plugin.player[it.guild!!.id]!!.playingTrack.info.title} at ${duration / 10} seconds.")
            }
        }
    }

    //TODO add play with no arguments as an invoker
    command("Resume") {
        description = "Continues the last song (If one is still queued)"
        requiresGuild = true
        execute {
            if (!plugin.player[it.guild!!.id]!!.isPaused){
                if (plugin.player[it.guild!!.id]!!.playingTrack != null){
                    it.respond("The song is already playing.")
                }else{
                    it.respond("No songs are currently queued.")
                }
            }else {
                plugin.player[it.guild!!.id]!!.isPaused = false
                //TODO fix long to seconds display later
                val duration: Double = (plugin.player[it.guild!!.id]!!.playingTrack.position / 100).toDouble()
                it.respond("Resumed song: ${plugin.player[it.guild!!.id]!!.playingTrack.info.title} from ${duration / 10} seconds.")
            }
        }
    }

    //TODO add next as an alias
    //TODO person who queued the song can skip only or staff
    command("Skip") {
        description = "Skips the current song."
        requiresGuild = true
        execute {
            if (plugin.queue.isNullOrEmpty()) {
                it.respond("No songs currently queued.")
            } else {
                it.respond("Skipped song: ${plugin.player[it.guild!!.id]!!.playingTrack.info.title} by ${plugin.player[it.guild!!.id]!!.playingTrack.info.author}")
                plugin.startNextTrack(it.guild!!.id, false)
            }
        }
    }

    command("Restart") {
        description = "Replays the current song from the beginning."
        requiresGuild = true
        execute {
            plugin.player[it.guild!!.id]!!.playingTrack.position = 0
            it.respond("Restarted the song: ${plugin.player[it.guild!!.id]!!.playingTrack.info.title}")
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
            plugin.player[it.guild!!.id]!!.volume = it.args.component1() as Int
        }
    }

    var previousVolume: MutableMap<String, Int> = mutableMapOf()

    command("Mute") {
        description = "Mute bot, but keeps it playing."
        requiresGuild = true
        execute {
            if (previousVolume.containsKey(it.guild!!.id)) {
                it.respond("The bot is already muted.")
            } else {
                previousVolume[it.guild!!.id] = plugin.player[it.guild!!.id]!!.volume
                plugin.player[it.guild!!.id]!!.volume = 0
                it.respond("The bot is now muted.")
            }
        }
    }

    command("Unmute") {
        description = "Sets bot's volume back to previous level before it was muted."
        requiresGuild = true
        execute {
            if (previousVolume.containsKey(it.guild!!.id)) {
                plugin.player[it.guild!!.id]!!.volume = previousVolume[it.guild!!.id]!!
                previousVolume.remove(it.guild!!.id)
                it.respond("The bot is now unmuted.")
            } else {
                it.respond("The bot is currently not muted - check the volume level.")
            }
        }
    }
}