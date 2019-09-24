package io.github.cferg.musicbot.services

import io.github.cferg.musicbot.data.Configuration
import io.github.cferg.musicbot.utility.Constants.Companion.MANAGEMENT_CATEGORY
import io.github.cferg.musicbot.utility.Constants.Companion.MODERATION_CATEGORY
import me.aberrantfox.kjdautils.api.annotation.Service
import me.aberrantfox.kjdautils.api.dsl.Command
import me.aberrantfox.kjdautils.discord.Discord
import me.aberrantfox.kjdautils.extensions.jda.toMember
import net.dv8tion.jda.api.entities.*

@Service
class PermissionsService(private val config: Configuration, discord: Discord) {
    init {
        discord.configuration.visibilityPredicate = { command: Command, user: User, _: MessageChannel, guild: Guild? ->
            when {
                command.category == MANAGEMENT_CATEGORY -> user.toMember(guild!!)?.isOwner ?: false
                command.category == MODERATION_CATEGORY -> user.toMember(guild!!)?.roles?.any {it.id == config.guildConfigurations[guild.id]?.staffRole} ?: false
                else -> true
            }
        }
    }
}