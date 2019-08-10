import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import me.aberrantfox.kjdautils.api.KUtils
import me.aberrantfox.kjdautils.api.startBot
import services.Handler
import services.RichPresence

var plugin = Main()
var disc: KUtils? = null
var token:String = ""

class Main {
    var playerManager: AudioPlayerManager = DefaultAudioPlayerManager()
    var player: AudioPlayer
    var handler: Handler

    init {
        AudioSourceManagers.registerRemoteSources(playerManager)
        AudioSourceManagers.registerLocalSource(playerManager)

        player = playerManager.createPlayer()

        handler = Handler()

        player.addListener(handler)
    }
}

fun main(Args: Array<String>) {
    token = Args.first()

    disc = startBot(token) {
        configure {
            prefix = "$"
        }
    }

    //RichPresence()
}