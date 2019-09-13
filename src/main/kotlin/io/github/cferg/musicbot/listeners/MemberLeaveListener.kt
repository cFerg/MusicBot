package io.github.cferg.musicbot.listeners

import com.google.common.eventbus.Subscribe
import io.github.cferg.musicbot.data.Configuration
import io.github.cferg.musicbot.extensions.clearByMember
import me.aberrantfox.kjdautils.extensions.jda.fullName
import net.dv8tion.jda.api.events.guild.member.GuildMemberLeaveEvent

class MemberLeaveListener(private val config: Configuration) {
    @Subscribe
    fun onGuildMemberLeave(event: GuildMemberLeaveEvent) {
        with(event) {
            val songList = guild.clearByMember(user.id)

            if (songList.isNotEmpty()) {
                val guildConfig = config.guildConfigurations[guild.id] ?: return
                val textChannel = guild.getTextChannelById(guildConfig.loggingChannelID) ?: return

                var display = "Removed all songs from ${user.fullName()} | ID: ${user.idLong} :"

                songList.forEach {song ->
                    with(song.track.info) {
                        display += "\n    -`${title}` by `${author}` | `${uri}`"
                    }
                }

                textChannel.sendMessage(display).queue()
            }
        }
    }
}
