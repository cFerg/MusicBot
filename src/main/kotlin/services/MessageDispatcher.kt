package services

import java.util.function.Consumer
import net.dv8tion.jda.core.entities.Message


interface MessageDispatcher {
    fun sendMessage(message: String, success: Consumer<Message>, failure: Consumer<Throwable>)

    fun sendMessage(message: String)
}