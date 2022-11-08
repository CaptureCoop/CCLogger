package org.capturecoop.cclogger

import java.time.LocalDateTime

class CCLogMessage(var level: CCLogLevel, var message: String, var time: LocalDateTime, var stackTraceElement: StackTraceElement?, var isException: Boolean) { }