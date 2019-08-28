package io.github.cferg.musicbot.extensions

fun Long.toTimeString(): String {
    val seconds = this / 1000
    val minutes = seconds / 60

    return "${minutes % 60}:${seconds % 60}"
}