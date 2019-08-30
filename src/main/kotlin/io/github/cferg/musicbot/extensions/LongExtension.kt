package io.github.cferg.musicbot.extensions

fun Long.toTimeString(): String {
    val seconds = this / 1000
    val minutes = seconds / 60
    val secRemainder = seconds % 60
    val minRemainder = minutes % 60
    val secDisplay = if (secRemainder < 10) "0${secRemainder}" else "$secRemainder"

    return "$minRemainder:$secDisplay"
}