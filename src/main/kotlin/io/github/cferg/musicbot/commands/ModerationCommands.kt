package io.github.cferg.musicbot.commands

import io.github.cferg.musicbot.data.Configuration
import io.github.cferg.musicbot.services.AudioPlayerService
import io.github.cferg.musicbot.services.EmbedTrackListService
import me.aberrantfox.kjdautils.api.dsl.CommandSet
import me.aberrantfox.kjdautils.api.dsl.arg
import me.aberrantfox.kjdautils.api.dsl.commands
import me.aberrantfox.kjdautils.extensions.jda.sendPrivateMessage
import me.aberrantfox.kjdautils.internal.arguments.MemberArg
import me.aberrantfox.kjdautils.internal.di.PersistenceService
import net.dv8tion.jda.api.entities.Member

@CommandSet("Moderation")
fun moderationCommands(plugin: AudioPlayerService, config: Configuration, persistenceService: PersistenceService, embed: EmbedTrackListService) = commands {
    command("Ignore") {
        description = "Add the member to a bot blacklist."
        requiresGuild = true
        expect(arg(MemberArg, false))
        execute {
            val member = it.args.component1() as Member

            if (config.guildConfigurations[it.guild!!.id]!!.ignoreList.contains(member.id)) {
                it.author.sendPrivateMessage("${member.effectiveName} is already in the bot blacklist.")
                return@execute
            }

            config.guildConfigurations[it.guild!!.id]!!.ignoreList.add(member.id)
            persistenceService.save(config)
            it.author.sendPrivateMessage("${member.effectiveName} is now added to the bot blacklist.")
        }
    }

    command("Unignore") {
        description = "Removes the member from a bot blacklist."
        requiresGuild = true
        expect(arg(MemberArg, false))
        execute {
            val member = it.args.component1() as Member

            if (config.guildConfigurations[it.guild!!.id]!!.ignoreList.contains(member.id)) {
                config.guildConfigurations[it.guild!!.id]!!.ignoreList.remove(member.id)
                persistenceService.save(config)
                it.author.sendPrivateMessage("${member.effectiveName} is now removed from the bot blacklist.")
            } else {
                it.author.sendPrivateMessage("${member.effectiveName} is not currently in the bot blacklist.")
            }
        }
    }
}