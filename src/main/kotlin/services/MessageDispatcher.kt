package services

import net.dv8tion.jda.api.entities.Message
import java.util.function.Consumer

//TODO delete
interface MessageDispatcher {
    fun sendMessage(message: String, success: Consumer<Message>, failure: Consumer<Throwable>)

    fun sendMessage(message: String)
}