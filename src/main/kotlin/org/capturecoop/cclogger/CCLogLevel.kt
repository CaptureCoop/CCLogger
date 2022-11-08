package org.capturecoop.cclogger

enum class CCLogLevel(val htmlColor: String, val ansiColor: String) {
    DEBUG("purple", "\u001b[1;95m"),
    INFO("white", "\u001b[0;97m"),
    WARNING("yellow", "\u001B[0;93m"),
    ERROR("red", "\u001b[0;91m");
}

fun CCLogLevel.ansiReset() = "\u001B[0m"