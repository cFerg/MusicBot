package io.github.cferg.musicbot.preconditions

import io.github.cferg.musicbot.data.Configuration
import me.aberrantfox.kjdautils.api.dsl.*
import me.aberrantfox.kjdautils.internal.command.*

@Precondition(0)
fun isConfigured(config: Configuration) = exit@{ event: CommandEvent ->
    val guild = event.guild ?: return@exit Fail("**Failure:** This command must be ran in a guild.")

    if (event.commandStruct.commandName.equals("Setup", true)) return@exit Pass

    if (guild.id in config.guildConfigurations) return@exit Pass

    return@exit Fail("Please have ${guild.owner!!.asMention} run the Setup command.\n" +
        "They'll need to provide:\n" +
        "A Role's Name for Staff Perms\n" +
        "A Text Channel ID for a Logging Channel\n")
}