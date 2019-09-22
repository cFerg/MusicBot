package io.github.cferg.musicbot.extensions

fun Long.toTimeString(): String {
    val seconds = this / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val secRemainder = seconds % 60
    val minRemainder = minutes % 60
    val hourRemainder = hours % 24

    return if (minutes > 60){
        val minDisplay = if (minRemainder < 10) "0${minRemainder}" else "$minRemainder"

        "${hourRemainder}h : ${minDisplay}m"
    }else {
        val secDisplay = if (secRemainder < 10) "0${secRemainder}" else "$secRemainder"

        "${minRemainder}m : ${secDisplay}s"
    }
}