package commands

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import me.aberrantfox.kjdautils.api.dsl.CommandSet
import me.aberrantfox.kjdautils.api.dsl.arg
import me.aberrantfox.kjdautils.api.dsl.commands
import me.aberrantfox.kjdautils.extensions.jda.toMember
import me.aberrantfox.kjdautils.internal.command.arguments.UrlArg
import net.dv8tion.jda.api.entities.VoiceChannel
import net.dv8tion.jda.api.managers.AudioManager
import plugin
import services.AudioPlayerSendHandler

//TODO move sending handler, voice channel, audio manager, and initial connector out of the commands, but in an initializer
@CommandSet("player")
fun playCommand() = commands {
    command("play") {
        description = "Play the song listed - If a song is already playing, it's added to a queue."
        expect(arg(UrlArg))
        execute {
            val url = it.args.component1() as String
            val vc: VoiceChannel = it.author.toMember(it.guild!!)!!.voiceState!!.channel!!
            val am: AudioManager = it.guild!!.audioManager
            am.sendingHandler = AudioPlayerSendHandler(plugin.player)
            am.openAudioConnection(vc)

            plugin.playerManager.loadItem(url, object : AudioLoadResultHandler {
                override fun trackLoaded(track: AudioTrack) {
                    it.respond("Successfully loaded the track.")
                    plugin.handler.queue(track)
                    plugin.player.playTrack(track)
                }

                override fun playlistLoaded(playlist: AudioPlaylist) {
                    for (track in playlist.tracks) {
                        plugin.handler.queue(track)
                    }
                }

                override fun noMatches() = it.respond("No matching song found")

                override fun loadFailed(throwable: FriendlyException) = it.respond("Error, could not load track.")
            })
        }
    }
}