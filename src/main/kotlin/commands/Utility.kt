package commands

import me.aberrantfox.kjdautils.api.dsl.CommandSet
import me.aberrantfox.kjdautils.api.dsl.commands

@CommandSet("utility")
fun pingCommand() = commands {
    command("ping") {
        description = "Responds with pong!"
        execute {
            it.respond("pong!")
        }
    }
}