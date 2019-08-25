package io.github.cferg.musicbot.data

import me.aberrantfox.kjdautils.api.annotation.Data

@Data("config/config.json")
data class Configuration(val botOwner: String = "insert-id",
                         var prefix: String = "$",
                         val guildConfigurations: MutableList<GuildConfig> = mutableListOf(GuildConfig())) {
                            fun getGuildConfig(guildID: String) = guildConfigurations.firstOrNull {
                                it.guildID == guildID
                            }
                        }

data class GuildConfig(val guildID: String = "insert-id", var requiredRole: String = "Staff")