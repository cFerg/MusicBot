package io.github.cferg.musicbot.services

import com.google.gson.Gson
import io.github.cferg.musicbot.data.Configuration
import me.aberrantfox.kjdautils.api.annotation.Service
import me.aberrantfox.kjdautils.api.dsl.embed
import me.aberrantfox.kjdautils.extensions.jda.fullName
import net.dv8tion.jda.api.entities.Guild
import java.awt.Color

@Service
class InfoService(private val configuration: Configuration) {
    private data class Properties(val version: String, val author: String, val repository: String)

    private val propFile = Properties::class.java.getResource("/properties.json").readText()
    private val project = Gson().fromJson(propFile, Properties::class.java)

    private val version = project.version
    private val author = project.author
    val source = project.repository

    fun botInfo(guild: Guild) = embed {
        val self = guild.jda.selfUser

        color = Color.green
        thumbnail = self.effectiveAvatarUrl
        addField(self.fullName(), "A multi-guild music bot for discord.")
        addInlineField("Version", version)
        addInlineField("Prefix", configuration.prefix)
        addInlineField("Author", "[$author](https://discordapp.com/users/167417801873555456/)")
        addInlineField("Contributors",
                "[JakeyWakey#1569](https://discordapp.com/users/254786431656919051/)\n" +
                "[Elliott#0001](https://discordapp.com/users/335628039302021121/)")
        addField("Source", "[$source]($source)")
    }
}