package services

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import me.aberrantfox.kjdautils.api.annotation.Service

@Service
class ManagerService {
    var playerManager: AudioPlayerManager = DefaultAudioPlayerManager()
    var player: AudioPlayer
    var handler: Handler

    init {
        AudioSourceManagers.registerRemoteSources(playerManager)
        AudioSourceManagers.registerLocalSource(playerManager)

        player = playerManager.createPlayer()

        handler = Handler(this)

        player.addListener(handler)
    }
}