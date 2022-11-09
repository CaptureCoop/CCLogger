package org.capturecoop.cclogger

import java.awt.Color

enum class CCLogFilter {
    NONE, //Display nothing
    ERRORS, //Display errors
    WARNINGS, //Display warnings and errors
    INFO, //Display info, warnings and errors
    DEBUG //Display everything + debug messages that provide extra info
}

enum class CCLogLevel(val color: Color) {
    DEBUG(Color.PINK),
    INFO(Color.WHITE),
    WARNING(Color.YELLOW),
    ERROR(Color.RED);
    fun isNot(vararg levels: CCLogLevel): Boolean {
        levels.forEach { if(it == this) return false }
        return true
    }

    fun getAnsiColor() = color.toAnsi()
    fun getHTMLColor() = "rgb(${color.red}, ${color.green}, ${color.blue})"
}

fun Color.toAnsi() = "\u001B[38;2;${red};${green};${blue}m"

fun CCLogLevel.ansiReset() = "\u001B[0m"