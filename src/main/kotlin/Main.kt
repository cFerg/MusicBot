import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import me.aberrantfox.kjdautils.api.startBot
import services.Handler

var plugin = Main()

class Main {
    var playerManager: AudioPlayerManager = DefaultAudioPlayerManager()
    var player: AudioPlayer
    var handler: Handler

    init {
        //Music Related
        //=======================================================================

        //for remote tracks ie) urls
        AudioSourceManagers.registerRemoteSources(playerManager)

        //for local tracks from disk - might use for presets
        //AudioSourceManagers.registerLocalSource(playerManager)

        player = playerManager.createPlayer()

        handler = Handler()

        player.addListener(handler)
    }
}

fun main(Args: Array<String>) {
    //Bot Related
    //=======================================================================
    val token = Args.first()

    startBot(token) {
        configure {
            prefix = "$"
        }
    }
}