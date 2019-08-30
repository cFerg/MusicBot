# Commands

## Key
| Symbol     | Meaning                    |
| ---------- | -------------------------- |
| (Argument) | This argument is optional. |

## Management
| Commands   | Arguments | Description                                    |
| ---------- | --------- | ---------------------------------------------- |
| Disconnect | <none>    | Remove the bot from its current voice channel. |
| Prefix     | Character | Sets the prefix for the bot.                   |
| SetStaff   | Role      | Sets a Staff role for moderation commands      |

## Player
| Commands | Arguments       | Description                                                                 |
| -------- | --------------- | --------------------------------------------------------------------------- |
| Clear    | <none>          | Removes all currently queued songs.                                         |
| Display  | <none>          | Displays the current track.                                                 |
| Ignore   | Member          | Add the member to a bot blacklist.                                          |
| Mute     | (Member)        | Mute bot, but keeps it playing.                                             |
| Pause    | <none>          | Pauses the current song.                                                    |
| Play     | URL             | Play the song listed - If a song is already playing, it's added to a queue. |
| Restart  | <none>          | Replays the current song from the beginning.                                |
| Resume   | <none>          | Continues the last song (If one is still queued)                            |
| Skip     | <none>          | Skips the current song.                                                     |
| Unignore | Member          | Removes the member from a bot blacklist.                                    |
| Unmute   | (Member)        | Sets bot's volume back to previous level before it was muted.               |
| Volume   | Integer (0-100) | Adjust volume from range 0-100                                              |

## Utility
| Commands | Arguments | Description                                |
| -------- | --------- | ------------------------------------------ |
| BotInfo  | <none>    | Displays the bot information.              |
| Ping     | <none>    | Displays network ping of the bot!          |
| Source   | <none>    | Display the (source code) repository link. |
| help     | (Word)    | Display a help menu                        |

