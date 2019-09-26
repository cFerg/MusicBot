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
        discord.configuration.visibilityPredicate = predicate@{ command: Command, user: User, _: MessageChannel, guild: Guild? ->
            guild ?: return@predicate false
            val guildConfiguration = config.guildConfigurations[guild.id] ?: return@predicate false
            val member = user.toMember(guild)!!

            when (command.category) {
                MANAGEMENT_CATEGORY -> member.isOwner
                MODERATION_CATEGORY -> member.roles.any { it.id == guildConfiguration.staffRole }
                else -> true
            }
        }
    }
}