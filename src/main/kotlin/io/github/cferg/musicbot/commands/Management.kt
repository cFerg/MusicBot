package io.github.cferg.musicbot.commands

import io.github.cferg.musicbot.data.ChannelGroup
import io.github.cferg.musicbot.data.VoicePair
import io.github.cferg.musicbot.data.Channels
import me.aberrantfox.kjdautils.api.dsl.CommandSet
import me.aberrantfox.kjdautils.api.dsl.arg
import me.aberrantfox.kjdautils.api.dsl.commands
import me.aberrantfox.kjdautils.internal.arguments.ChoiceArg
import me.aberrantfox.kjdautils.internal.arguments.TextChannelArg
import me.aberrantfox.kjdautils.internal.arguments.VoiceChannelArg
import me.aberrantfox.kjdautils.internal.di.PersistenceService
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.VoiceChannel
import io.github.cferg.musicbot.utility.AudioPlayerSendHandler
import io.github.cferg.musicbot.services.AudioPlayerService

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
            val vc = it.args.component1() as VoiceChannel
            val tc = it.args.component2() as TextChannel

            if(channels.channelGroups.containsKey(it.guild!!.id)) {
                if (!channels.getVoicePair(it.guild!!.id).isNullOrEmpty()) {
                    channels.getVoicePair(it.guild!!.id).add(VoicePair(vc.id, tc.id))
                    persistenceService.save(channels)

                    it.respond("Channel pair linked!")
                    return@execute
                }
            }

            channels.channelGroups.putIfAbsent(it.guild!!.id, ChannelGroup("", mutableListOf(VoicePair(vc.id, tc.id))))
            persistenceService.save(channels)

            it.respond("Channel pair linked!")
        }
    }

    command("Unlink") {
        description = "Un-Links a text and voice channel."
        requiresGuild = true
        expect(VoiceChannelArg, TextChannelArg)
        execute {
            if (!channels.getVoicePair(it.guild!!.id).isNullOrEmpty()) {
                val guild = it.guild!!.id
                val vc = it.args.component1() as VoiceChannel
                val tc = it.args.component2() as TextChannel

                if (channels.hasVoiceChannel(guild, vc.id) && channels.hasTextChannel(guild, tc.id)) {
                    for (i in channels.getVoicePair(guild)) {
                        if (i.voiceChannelID == vc.id && i.textChannelID == tc.id) {
                            channels.getVoicePair(guild).remove(i)
                            persistenceService.save(channels)

                            it.respond("Unlinked the pair of channels.")
                            return@execute
                        }
                    }
                    it.respond("No channels paired with the arguments given.")

                } else {
                    it.respond("No channels paired with the arguments given.")
                }
            } else {
                it.respond("No channels paired under this guild.")
            }
        }
    }

    command("Log") {
        description = "Sets or Removes a logging channel for the bot."
        requiresGuild = true
        expect(arg(ChoiceArg("set / remove", "add", "set", "delete", "remove"), false), arg(TextChannelArg, true, ""))
        execute {
            val choice = it.args.component1() as String

            if (choice.equals("add", ignoreCase = true) || choice.equals("set", ignoreCase = true)){
                if (it.args.component2() is String){
                    it.respond("Please add a valid text channel using: \$\$Log add <TextChannelID>")
                    return@execute
                }

                val tc = it.args.component2() as TextChannel
                if(channels.channelGroups.containsKey(it.guild!!.id)){
                    channels.channelGroups[it.guild!!.id]!!.loggingChannelID = tc.id
                    persistenceService.save(channels)
                    it.respond("Added a logging channel for voice activity.")
                }else{
                    channels.channelGroups[it.guild!!.id] = ChannelGroup(tc.id, mutableListOf())
                    it.respond("Added a logging channel for voice activity.")
                    it.respond("Please create a voice/text channel pair now, using \$\$Link <VoiceChannelID> <TextChannelID>")
                }
            }else if(choice.equals("remove", ignoreCase = true) || choice.equals("delete", ignoreCase = true)){
                if(channels.channelGroups.containsKey(it.guild!!.id)){
                    channels.channelGroups[it.guild!!.id]!!.loggingChannelID = ""
                    persistenceService.save(channels)
                    it.respond("Logging channel successfully removed.")
                }else{
                    it.respond("No channel grouping set up - You have nothing to remove.")
                }
            }else{
                it.respond("Please use one of the appropriate io.github.cferg.musicbot.commands.\nFor Setting the Logging Channel\$\$Log Add/Set <TextChannelID>\nFor Removing the logging channel:\$\$Log Remove/Delete <TextChannelID>")
            }
        }
    }
}