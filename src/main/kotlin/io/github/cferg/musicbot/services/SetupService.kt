package io.github.cferg.musicbot.services

import io.github.cferg.musicbot.data.Configuration
import me.aberrantfox.kjdautils.api.annotation.Service
import me.aberrantfox.kjdautils.discord.Discord
import me.aberrantfox.kjdautils.internal.services.ConversationService
import net.dv8tion.jda.api.entities.Guild
import java.util.Timer
import kotlin.concurrent.schedule

@Service
class SetupService(config: Configuration, discord: Discord, private val conversationService: ConversationService) {
    private val guildConfigureList = mutableListOf<Guild>()

    init {
        Timer("SettingUp", false).schedule(1500) {
            //Separates out guilds needed to be configured, to avoid halting
            for (i in discord.jda.guilds){
                if (i.id !in config.guildConfigurations){
                    guildConfigureList.add(i)
                }
            }

            runSetup()
        }
    }

    private fun runSetup(){
        val guild = guildConfigureList.firstOrNull() ?: return

        println("Started configuration of ${guild.name}")
        val owner = guild.owner!!.user

        conversationService.createConversation(owner, guild, "setupConversation")

        guildConfigureList.remove(guild)
        runSetup()
    }
}