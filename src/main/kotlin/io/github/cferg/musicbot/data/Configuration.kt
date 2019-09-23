package io.github.cferg.musicbot.data

import me.aberrantfox.kjdautils.api.annotation.Data

@Data("config/config.json")
data class Configuration(var prefix: String = "$",
                         var guildConfigurations: MutableMap<String, GuildInfo> = mutableMapOf())

data class GuildInfo(var staffRole: String = "",
                     var loggingChannelID: String = "",
                     var playlistRole: String = "",
                     var playlistQueueLimit: Int = 0,
                     var songMaxDuration: Long = 0,
                     var songQueueLimit: Int = 0,
                     var ignoreList: MutableList<String> = mutableListOf())