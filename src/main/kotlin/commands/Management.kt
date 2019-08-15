package commands

import data.Channels
import me.aberrantfox.kjdautils.api.dsl.CommandSet
import me.aberrantfox.kjdautils.api.dsl.arg
import me.aberrantfox.kjdautils.api.dsl.commands
import me.aberrantfox.kjdautils.internal.arguments.VoiceChannelArg
import net.dv8tion.jda.api.entities.VoiceChannel
import utility.AudioPlayerSendHandler
import services.ManagerService

@CommandSet("Management")
fun managementCommands(plugin: ManagerService, channels: Channels) = commands {
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
        }
    }

    command("Disconnect"){
        description = "Remove the bot from its current voice channel."
        requiresGuild = true
        execute{
            val manager = it.guild!!.audioManager
            plugin.player.stopTrack()
            manager.closeAudioConnection()
        }
    }
}