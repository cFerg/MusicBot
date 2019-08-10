package services

import disc
import net.dv8tion.jda.internal.JDAImpl
import net.dv8tion.jda.internal.requests.WebSocketCode
import org.json.JSONObject

//TODO remove/replace with kutils variant
fun RichPresence(){
    val obj = JSONObject()
    val gameObj = JSONObject()

    val jdaImpl = disc!!.discord.jda as JDAImpl

    gameObj.put("name", "Boom Bot")
    gameObj.put("type", 2)
    gameObj.put("details", "A Music Bot for Voice Channels.")
    gameObj.put("state", "Not Currently Playing")

    val assetsObj = JSONObject()
    assetsObj.put("large_image", "default")
    assetsObj.put("large_text", "Boom Bot")

    gameObj.put("assets", assetsObj)

    obj.put("game", gameObj)
    obj.put("afk", false)
    obj.put("status", "online")
    obj.put("since", System.currentTimeMillis())

    println(obj)

    jdaImpl.client.send(JSONObject().put("d", obj).put("op", WebSocketCode.PRESENCE).toString())
}