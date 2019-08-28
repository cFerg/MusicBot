package io.github.cferg.musicbot.data

import me.aberrantfox.kjdautils.api.annotation.Data

@Data("config/config.json")
data class Configuration(var prefix: String = "$",
                         var guildConfigurations: MutableMap<String, GuildInfo> = mutableMapOf())

data class GuildInfo(var staffRole: String = "insert-role-id",
                     var loggingChannelID: String = "insert-channel-ID",
                     var ignoreList: MutableList<String> = mutableListOf())