package commands

import data.ChannelPair
import data.Channels
import me.aberrantfox.kjdautils.api.dsl.CommandSet
import me.aberrantfox.kjdautils.api.dsl.arg
import me.aberrantfox.kjdautils.api.dsl.commands
import me.aberrantfox.kjdautils.internal.arguments.TextChannelArg
import me.aberrantfox.kjdautils.internal.arguments.VoiceChannelArg
import me.aberrantfox.kjdautils.internal.di.PersistenceService
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.VoiceChannel
import utility.AudioPlayerSendHandler
import services.AudioPlayerService

@CommandSet("Management")
fun managementCommands(plugin: AudioPlayerService, channels: Channels, persistenceService: PersistenceService) = commands {
    //TODO consider deprecating move command - it's not always safe
    command("Move") {
        description = "Move bot to the current voice channel or to a specified voice channel via ID."
        requiresGuild = true
        expect(arg(VoiceChannelArg, true) { channels.getVoiceChannel(it.guild!!.id, it.channel.id) })
        execute {
            if (it.args.component1() is Unit) {
                return@execute it.respond("Sorry, you need to either be in a channel or specify a valid channel ID")
            }

            val channel = it.args.component1() as VoiceChannel
            val manager = it.guild!!.audioManager
            manager.sendingHandler = AudioPlayerSendHandler(plugin.player[it.guild!!.id]!!)
            manager.openAudioConnection(channel)
        }
    }

    command("Disconnect") {
        description = "Remove the bot from its current voice channel."
        requiresGuild = true
        execute {
            val manager = it.guild!!.audioManager
            plugin.player[it.guild!!.id]!!.stopTrack()
            manager.closeAudioConnection()
        }
    }

    //TODO add a "pair already exists" check
    command("Link") {
        description = "Links a text and voice channel."
        requiresGuild = true
        expect(VoiceChannelArg, TextChannelArg)
        execute {
            if (!channels.getGuild(it.guild!!.id).isNullOrEmpty()) {
                val vc = it.args.component1() as VoiceChannel
                val tc = it.args.component2() as TextChannel

                channels.getGuild(it.guild!!.id).add(ChannelPair(vc.id, tc.id))

                persistenceService.save(channels)
                it.respond("Channel pair linked!")
            } else {
                val vc = it.args.component1() as VoiceChannel
                val tc = it.args.component2() as TextChannel

                channels.channelPairings.add(Pair(it.guild!!.id, mutableListOf(ChannelPair(vc.id, tc.id))))

                persistenceService.save(channels)
                it.respond("Channel pair linked!")
            }
        }
    }

    command("Unlink") {
        description = "Un-Links a text and voice channel."
        requiresGuild = true
        expect(VoiceChannelArg, TextChannelArg)
        execute {
            if (!channels.getGuild(it.guild!!.id).isNullOrEmpty()) {
                val guild = it.guild!!.id
                val vc = it.args.component1() as VoiceChannel
                val tc = it.args.component2() as TextChannel

                if (channels.hasVoiceChannel(guild,vc.id) && channels.hasTextChannel(guild,tc.id)){
                    for (i in channels.getGuild(guild)){
                        if (i.voiceChannelID == vc.id && i.textChannelID == tc.id){
                            channels.getGuild(guild).remove(i)

                            persistenceService.save(channels)
                            it.respond("Unlinked the pair of channels.")
                            return@execute
                        }
                    }
                    it.respond("No channels paired with the arguments given.")

                }else{
                    it.respond("No channels paired with the arguments given.")
                }
            }else{
                it.respond("No channels paired under this guild.")
            }
        }
    }
}