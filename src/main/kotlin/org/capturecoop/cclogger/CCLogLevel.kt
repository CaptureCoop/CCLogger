package org.capturecoop.cclogger

enum class CCLogFilter {
    NONE, //Display nothing
    ERRORS, //Display errors
    WARNINGS, //Display warnings and errors
    INFO, //Display info, warnings and errors
    DEBUG //Display everything + debug messages that provide extra info
}

enum class CCLogLevel(val htmlColor: String, val ansiColor: String) {
    DEBUG("purple", "\u001b[1;95m"),
    INFO("white", "\u001b[0;97m"),
    WARNING("yellow", "\u001B[0;93m"),
    ERROR("red", "\u001b[0;91m");
}

fun CCLogLevel.ansiReset() = "\u001B[0m"