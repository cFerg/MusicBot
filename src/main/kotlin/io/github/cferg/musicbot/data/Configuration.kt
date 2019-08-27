package io.github.cferg.musicbot.data

import me.aberrantfox.kjdautils.api.annotation.Data

@Data("config/config.json")
data class Configuration(val botOwner: String = "167417801873555456",
                         var prefix: String = "$",
                         var guildConfigurations: MutableMap<String, GuildRoles> = mutableMapOf())

data class GuildRoles(var djRole: String = "insert-role-id",
                      var manageRole: String = "insert-role-id",
                       var muteRole: String = "insert-role-id",
                       var staffRole: String = "insert-role-id")