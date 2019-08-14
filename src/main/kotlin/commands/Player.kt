package commands

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import data.Channels
import data.currentChannel
import data.currentVoice
import me.aberrantfox.kjdautils.api.dsl.CommandSet
import me.aberrantfox.kjdautils.api.dsl.arg
import me.aberrantfox.kjdautils.api.dsl.commands
import me.aberrantfox.kjdautils.internal.arguments.IntegerRangeArg
import me.aberrantfox.kjdautils.internal.arguments.UrlArg
import me.aberrantfox.kjdautils.internal.arguments.VoiceChannelArg
import net.dv8tion.jda.api.entities.VoiceChannel
import net.dv8tion.jda.api.managers.AudioManager
import services.AudioPlayerSendHandler
import services.ManagerService

@CommandSet("Player")
fun playerCommands(plugin: ManagerService, channels: Channels) = commands {
    command("Play") {
        description = "Play the song listed - If a song is already playing, it's added to a queue."
        requiresGuild = true
        expect(arg(UrlArg))
        execute {
            val url = it.args.component1() as String

            //TODO move to an audio connection event
            if (currentVoice == null){
                if (currentChannel == null){
                    currentVoice = it.guild!!.getVoiceChannelById(channels.getVoiceChannel(it.channel.id))
                    currentChannel = it.guild!!.getTextChannelById(channels.getTextChannel(currentVoice!!.id))
                }else{
                    currentVoice = it.guild!!.getVoiceChannelById(channels.getVoiceChannel(currentChannel!!.id))
                }
            }else if(currentVoice!!.id != channels.getVoiceChannel(it.channel.id)){
                currentVoice = it.guild!!.getVoiceChannelById(channels.getVoiceChannel(it.channel.id))
                currentChannel = it.guild!!.getTextChannelById(channels.getTextChannel(currentVoice!!.id))
            }

            val am: AudioManager = it.guild!!.audioManager
            am.sendingHandler = AudioPlayerSendHandler(plugin.player)
            am.openAudioConnection(currentVoice)

            plugin.playerManager.loadItem(url, object : AudioLoadResultHandler {
                override fun trackLoaded(track: AudioTrack) {
                    plugin.handler.queue(track)
                    it.respond("Added song: ${track.info.title} by ${track.info.author}")
                }

                override fun playlistLoaded(playlist: AudioPlaylist) {
                    for (track in playlist.tracks) {
                        plugin.handler.queue(track)
                        it.respond("Added song: ${track.info.title} by ${track.info.author}")
                    }
                }

                override fun noMatches() = it.respond("No matching song found")

                override fun loadFailed(throwable: FriendlyException) = it.respond("Error, could not load track.")
            })
        }
    }

    command("Move") {
        description = "Move bot to the current voice channel or to a specified voice channel via ID."
        requiresGuild = true
        expect(arg(VoiceChannelArg, true) { channels.getVoiceChannel(it.channel.id) })
        execute {
            if (it.args.component1() is Unit) {
                return@execute it.respond("Sorry, you need to either be in a channel or specify a valid channel ID")
            }

            val channel = it.args.component1() as VoiceChannel
            val manager = it.guild!!.audioManager
            manager.sendingHandler = AudioPlayerSendHandler(plugin.player)
            manager.openAudioConnection(channel)

            currentVoice = channel
            currentChannel = it.guild!!.getTextChannelById(channels.getTextChannel(currentVoice!!.id))
        }
    }

    command("Disconnect"){
        description = "Remove the bot from its current voice channel."
        requiresGuild = true
        execute{
            val manager = it.guild!!.audioManager
            plugin.player.stopTrack()
            manager.closeAudioConnection()

            currentVoice = null
            currentChannel = null
        }
    }

    command("Pause"){
        description = "Pauses the current song."
        requiresGuild = true
        execute{
            plugin.player.isPaused = true
            //TODO fix long to seconds display later
            val duration:Double = (plugin.player.playingTrack.position / 100).toDouble()
            it.respond("Paused song: ${plugin.player.playingTrack.info.title} at ${duration / 10} seconds.")
        }
    }

    command("Resume"){
        description = "Continues the last song (If one is still queued)"
        requiresGuild = true
        execute{
            plugin.player.isPaused = false
            //TODO fix long to seconds display later
            val duration:Double = (plugin.player.playingTrack.position / 100).toDouble()
            it.respond("Resumed song: ${plugin.player.playingTrack.info.title} from ${duration / 10} seconds.")
        }
    }

    //TODO add next as an alias
    command("Skip"){
        description = "Skips the current song."
        requiresGuild = true
        execute{
            if (plugin.handler.isQueueEmpty()){
                it.respond("No songs currently queued.")
            }else{
                it.respond("Skipped song: ${plugin.player.playingTrack.info.title} by ${plugin.player.playingTrack.info.author}")
                plugin.handler.skip()
            }
        }
    }

    command("Restart"){
        description = "Replays the current song from the beginning."
        requiresGuild = true
        execute{
            plugin.player.playingTrack.position = 0
            it.respond("Restarted the song: ${plugin.player.playingTrack.info.title}")
        }
    }

    command("Clear"){
        description = "Removes all currently queued songs."
        requiresGuild = true
        execute{
            plugin.handler.clearQueue()
            it.respond("Cleared the current list of songs.")
        }
    }

    command("Volume"){
        description = "Adjust volume from range 0-100"
        requiresGuild = true
        expect(IntegerRangeArg(min = 0, max = 100))
        execute{
            plugin.player.volume = it.args.component1() as Int
        }
    }

    var previousVolume:Int = plugin.player.volume

    command("Mute"){
        description = "Mute bot, but keeps it playing."
        requiresGuild = true
        execute{
            previousVolume = plugin.player.volume
            plugin.player.volume = 0
            it.respond("The bot is now muted.")
        }
    }

    command("Unmute"){
        description = "Sets bot's volume back to previous level before it was muted."
        requiresGuild = true
        execute{
            plugin.player.volume = previousVolume
            it.respond("The bot is now unmuted.")
        }
    }
}