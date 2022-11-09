package org.capturecoop.cclogger

import java.awt.Color
import java.time.LocalDateTime

data class CCLogMessage(
    val level: CCLogLevel,
    val message: String,
    val time: LocalDateTime,
    val stackTraceElement: StackTraceElement?,
    val isException: Boolean
)

enum class CCLogFilter {
    NONE, //Display nothing
    ERRORS, //Display errors
    WARNINGS, //Display warnings and errors
    INFO, //Display info, warnings and errors
    DEBUG //Display everything + debug messages that provide extra info
}

enum class CCLogLevel(private val color: Color) {
    DEBUG(Color.MAGENTA),
    INFO(Color.WHITE),
    WARNING(Color.YELLOW),
    ERROR(Color.RED);
    fun getAnsiColor() = "\u001B[38;2;${color.red};${color.green};${color.blue}m"
    fun getHTMLColor() = "rgb(${color.red}, ${color.green}, ${color.blue})"
    fun getAnsiReset() = "\u001B[0m"
    fun isNot(vararg levels: CCLogLevel): Boolean {
        levels.forEach { if(it == this) return false }
        return true
    }
}