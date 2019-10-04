# Commands

## Key
| Symbol     | Meaning                    |
| ---------- | -------------------------- |
| (Argument) | This argument is optional. |

## Management
| Commands          | Arguments                    | Description                                                                       |
| ----------------- | ---------------------------- | --------------------------------------------------------------------------------- |
| Disconnect        | <none>                       | Remove the bot from its current voice channel.                                    |
| Logging           | Text Channel Name            | Sets a Logging Channel to send bot command invokes to.                            |
| PlaylistLimit     | Song Limit                   | Sets a maximum playlist song limit \| Set to 0 for no limits.                     |
| PlaylistRole      | Role Name                    | Sets a role on who can add playlists                                              |
| PlaylistRoleClear | <none>                       | Removes a role, letting everyone add playlists.                                   |
| Prefix            | Prefix Character             | Sets the prefix for the bot.                                                      |
| Setup             | Role Name, Text Channel Name | Setups the configuration for a guild.                                             |
| SongDuration      | Time                         | Sets a maximum song duration limit \| Set to 0 for no limits.                     |
| SongLimit         | Song Limit                   | Sets how many songs a person can queue at a given time \| Set to 0 for no limits. |
| StaffRole         | Role Name                    | Sets a Staff role for moderation commands                                         |

## Moderation
| Commands | Arguments            | Description                                                   |
| -------- | -------------------- | ------------------------------------------------------------- |
| Clear    | <none>               | Removes all currently queued songs.                           |
| Hoist    | URL                  | Force the song to play, pushing the rest back one in queue.   |
| Ignore   | Member ID or Mention | Add the member to a bot blacklist.                            |
| Mute     | <none>               | Mute bot, but keeps it playing.                               |
| Restart  | <none>               | Replays the current song from the beginning.                  |
| Unignore | Member ID or Mention | Removes the member from a bot blacklist.                      |
| Unmute   | <none>               | Sets bot's volume back to previous level before it was muted. |
| Volume   | Range 0-100          | Adjust volume from range 0-100                                |

## Player
| Commands | Arguments                                 | Description                                                                 |
| -------- | ----------------------------------------- | --------------------------------------------------------------------------- |
| List     | <none>                                    | Lists the current songs.                                                    |
| Play     | URL                                       | Play the song listed - If a song is already playing, it's added to a queue. |
| Search   | YouTube \| YT \| SoundCloud \| SC, Search | Search a song based on keywords.                                            |
| Skip     | <none>                                    | Skips the current song.                                                     |

## Utility
| Commands | Arguments | Description                                |
| -------- | --------- | ------------------------------------------ |
| BotInfo  | <none>    | Displays the bot information.              |
| Help     | (Command) | Display a help menu.                       |
| Ping     | <none>    | Displays network ping of the bot!          |
| Source   | <none>    | Display the (source code) repository link. |

