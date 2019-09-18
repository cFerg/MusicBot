# Commands

## Key
| Symbol     | Meaning                    |
| ---------- | -------------------------- |
| (Argument) | This argument is optional. |

## Management
| Commands   | Arguments         | Description                                            |
| ---------- | ----------------- | ------------------------------------------------------ |
| Disconnect | <none>            | Remove the bot from its current voice channel.         |
| Prefix     | Character         | Sets the prefix for the bot.                           |
| SetLogging | TextChannel       | Sets a Logging Channel to send bot command invokes to. |
| SetStaff   | Role              | Sets a Staff role for moderation commands              |
| Setup      | Role, TextChannel | Setups the configuration for a guild.                  |

## Moderation
| Commands | Arguments       | Description                                                   |
| -------- | --------------- | ------------------------------------------------------------- |
| Clear    | <none>          | Removes all currently queued songs.                           |
| Hoist    | URL             | Force the song to play, pushing the rest back one in queue.   |
| Ignore   | Member          | Add the member to a bot blacklist.                            |
| Mute     | <none>          | Mute bot, but keeps it playing.                               |
| Restart  | <none>          | Replays the current song from the beginning.                  |
| Unignore | Member          | Removes the member from a bot blacklist.                      |
| Unmute   | <none>          | Sets bot's volume back to previous level before it was muted. |
| Volume   | Integer (0-100) | Adjust volume from range 0-100                                |

## Player
| Commands | Arguments | Description                                                                 |
| -------- | --------- | --------------------------------------------------------------------------- |
| List     | <none>    | Lists the current songs.                                                    |
| Play     | URL       | Play the song listed - If a song is already playing, it's added to a queue. |
| Skip     | <none>    | Skips the current song.                                                     |

## Utility
| Commands     | Arguments | Description                                |
| ------------ | --------- | ------------------------------------------ |
| BotInfo      | <none>    | Displays the bot information.              |
| ListCommands | <none>    | Lists all available commands.              |
| Ping         | <none>    | Displays network ping of the bot!          |
| Source       | <none>    | Display the (source code) repository link. |
| help         | (Word)    | Display a help menu                        |

