package io.github.cferg.musicbot.data

import me.aberrantfox.kjdautils.api.annotation.Data

@Data("config/config.json")
data class Configuration(var prefix: String = "$",
                         var guildConfigurations: MutableMap<String, GuildRoles> = mutableMapOf())

data class GuildRoles(var staffRole: String = "insert-role-id",
                      var ignoreList: MutableList<String> = mutableListOf())