package commands

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventListener
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import me.aberrantfox.kjdautils.api.dsl.CommandSet
import me.aberrantfox.kjdautils.api.dsl.arg
import me.aberrantfox.kjdautils.api.dsl.commands
import me.aberrantfox.kjdautils.extensions.jda.toMember
import me.aberrantfox.kjdautils.internal.command.arguments.UrlArg
import net.dv8tion.jda.core.entities.VoiceChannel
import net.dv8tion.jda.core.managers.AudioManager
import plugin
import services.AudioPlayerSendHandler
import java.lang.Compiler.command

@CommandSet("player")
fun playCommand() = commands {
    command("play") {
        description = "Play the song listed - if a song is already playing, it's added to a queue."
        expect(arg(UrlArg))
        execute {
            val url = it.args.component1() as String
            it.respond("successfully added the song from <$url>")
            val vc: VoiceChannel = it.author.toMember(it.guild!!).voiceState.channel
            val am: AudioManager = it.guild!!.audioManager
            am.sendingHandler = AudioPlayerSendHandler(plugin.player)
            am.openAudioConnection(vc)

            plugin.playerManager.loadItem(url, object : AudioLoadResultHandler {
                override fun trackLoaded(track: AudioTrack) {
                    plugin.handler.queue(track)
                    plugin.player.playTrack(track)
                }

                override fun playlistLoaded(playlist: AudioPlaylist) {
                    for (track in playlist.tracks) {
                        plugin.handler.queue(track)
                    }
                }

                override fun noMatches() {
                    // Notify the user that we've got nothing
                }

                override fun loadFailed(throwable: FriendlyException) {
                    // Notify the user that everything exploded
                }
            })
        }
    }
}