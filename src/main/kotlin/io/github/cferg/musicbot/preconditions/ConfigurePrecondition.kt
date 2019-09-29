package io.github.cferg.musicbot.preconditions

import io.github.cferg.musicbot.data.Configuration
import me.aberrantfox.kjdautils.api.dsl.*
import me.aberrantfox.kjdautils.internal.command.*

@Precondition(priority = 0)
fun isConfigured(config: Configuration) = precondition { event: CommandEvent ->
    val guild = event.guild ?: return@precondition Fail("**Failure:** This command must be ran in a guild.")

    if (event.commandStruct.commandName.equals("Setup", true)) return@precondition Pass

    if (guild.id in config.guildConfigurations) return@precondition Pass

    return@precondition Fail("Please have the owner run the `Setup` command.\n\n" +
        "They'll need to provide:\n" +
        "A `Role's Name` for Staff Perms\n" +
        "A `Text Channel ID` for a Logging Channel\n")
}