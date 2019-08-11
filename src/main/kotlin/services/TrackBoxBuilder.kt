package services

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

//TODO replace with embeds
object TrackBoxBuilder {
    private const val TOP_LEFT_CORNER = "\u2554"
    private const val TOP_RIGHT_CORNER = "\u2557"
    private const val BOTTOM_LEFT_CORNER = "\u255a"
    private const val BOTTOM_RIGHT_CORNER = "\u255d"
    private const val BORDER_HORIZONTAL = "\u2550"
    private const val BORDER_VERTICAL = "\u2551"
    private const val PROGRESS_FILL = "\u25a0"
    private const val PROGRESS_EMPTY = "\u2015"

    fun buildTrackBox(width: Int, track: AudioTrack, isPaused: Boolean, volume: Int): String {
        return boxify(width, buildFirstLine(width - 4, track), buildSecondLine(width - 4, track, isPaused, volume))
    }

    private fun buildFirstLine(width: Int, track: AudioTrack): String {
        val title = track.info.title
        val titleWidth = width - 7

        return buildString{
            if (title.length > titleWidth) {
                append(title.substring(0, titleWidth - 3))
                append("...")
            } else {
                append(title)
            }
        }
    }

    private fun buildSecondLine(width: Int, track: AudioTrack, isPaused: Boolean, volume: Int): String {
        val cornerText = if (isPaused) "PAUSED" else "$volume%"
        val duration = formatTiming(track.duration, track.duration)
        val position = formatTiming(track.position, track.duration)
        val spacing = duration.length - position.length
        val barLength = width - duration.length - position.length - spacing - 14
        val progress = min(track.position, track.duration).toFloat() / max(track.duration, 1).toFloat()
        val progressBlocks = (progress * barLength).roundToInt()

        return buildString {
            for (i in 0 until 6 - cornerText.length) {
                append(" ")
            }

            append("$cornerText [")

            for (i in 0 until barLength) {
                append(if (i < progressBlocks) PROGRESS_FILL else PROGRESS_EMPTY)
            }

            append("]")

            for (i in 0 until spacing + 1) {
                append(" ")
            }

            append("$position of $duration $TOP_RIGHT_CORNER")
        }
    }

    private fun formatTiming(timing: Long, maximum: Long): String {
        var timing = timing
        timing = min(timing, maximum) / 1000
        val seconds = timing % 60
        timing /= 60
        val minutes = timing % 60
        timing /= 60
        val hours = timing

        return if (maximum >= 3600000L) {
            String.format("%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%d:%02d", minutes, seconds)
        }
    }

    private fun boxifyLine(builder: StringBuilder, line: String) = builder.append("$BORDER_VERTICAL $line\n")

    private fun boxify(width: Int, firstLine: String, secondLine: String) = buildString {
        append("```$TOP_LEFT_CORNER")

        for (i in 0 until width - 1) {
            append(BORDER_HORIZONTAL)
        }

        appendln()

        boxifyLine(this, firstLine)
        boxifyLine(this, secondLine)

        append(BOTTOM_LEFT_CORNER)

        for (i in 0 until width - 2) {
            append(BORDER_HORIZONTAL)
        }

        append("$BOTTOM_RIGHT_CORNER```")
    }
}