import me.aberrantfox.kjdautils.api.startBot

fun main(args: Array<String>) {
    val token = args.firstOrNull() ?: return println("Missing token!")

    startBot(token) {
        configure {
            container.commands.getValue("help").category = "Utility"
        }
    }
}