package org.capturecoop.cclogger

import org.apache.commons.lang3.exception.ExceptionUtils
import org.apache.commons.text.StringEscapeUtils
import org.capturecoop.ccutils.utils.CCStringUtils
import java.io.File
import java.io.IOException
import java.lang.Exception
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.time.LocalDateTime

class CCLogger {
    companion object {
        var enabled = false
        private var paused = false
        var logFile: File? = null
            set(value) {
                value?.createNewFile()
                field = value
                refreshPreFileMessages()
            }
        private val MAX_LEVEL_LENGTH = CCLogLevel.WARNING.toString().length
        private val preFileMessages = ArrayList<String>() //These are printed but not written right away
        private val pausedMessages = ArrayList<CCLogMessage>() //These need to be constructed once ready
        var logFormat = "[%hour%:%minute%:%second%:%ms%] [%level%]%levelspace% [%filename%.%method%:%line%]: %message%"
        var htmlLog = ""
        var gitHubCodePathURL: String? = null //Example: https://github.com/CaptureCoop/SnipSniper/tree/<HASH HERE>/src/main/java/"
        var gitHubCodeClassPath: String? = null //Example: org.snipsniper -> If set only messages that contain this classpath get the @ link
        private var console: CCDebugConsole? = null
        private const val THIS_CLASSPATH = "org.capturecoop.cclogger.CCLogger"

        var logLevel = CCLogLevel.INFO

        //The reason for this is that this way we can take index 3 of stack trace at all times
        private fun logInternal(message: String, level: CCLogLevel, time: LocalDateTime, currentStackTrace: StackTraceElement) {
            if(!enabled)
                return

            if(level == CCLogLevel.DEBUG && logLevel == CCLogLevel.DEBUG)
                return

            var msg = CCStringUtils.formatDateTimeString(logFormat, time)

            var levelString = level.toString()

            if(levelString.length <= MAX_LEVEL_LENGTH) {
                msg = msg.toString().replace("%levelspace%", " ".repeat(MAX_LEVEL_LENGTH - levelString.length))
            } else {
                levelString = levelString.substring(0, MAX_LEVEL_LENGTH)
                msg = msg.toString().replace("%levelspace%", "")
            }

            var classFilename = currentStackTrace.fileName ?: "No file name"
            classFilename = classFilename.replace(".java" ,"")

            msg = msg.toString().replace("%filename%", classFilename)
            msg = msg.toString().replace("%method%", currentStackTrace.methodName)
            msg = msg.toString().replace("%line%", currentStackTrace.lineNumber.toString())

            msg = msg.toString().replace("%levelspace%", "")
            msg = msg.toString().replace("%level%", levelString)
            msg = msg.toString().replace("%message%", message)

            println(msg.toString().replace("%newline%", "\n"))

            var finalMsg = StringEscapeUtils.escapeHtml4(msg.toString()).replace(" ", "&nbsp")
            finalMsg = finalMsg.replace("%newline%", "<br>")
            if(gitHubCodePathURL != null && (gitHubCodeClassPath == null || gitHubCodeClassPath.isNullOrEmpty() || currentStackTrace.className.contains(gitHubCodeClassPath!!))) {
                val link = gitHubCodePathURL + currentStackTrace.className.replace("\\.", "/") + ".java#L" + currentStackTrace.lineNumber
                finalMsg = finalMsg.replace(":" + currentStackTrace.lineNumber + "]", ":" + currentStackTrace.lineNumber + " <a href='" + link + "'>@</a>]")
            }
            val htmlLine = "<p style='margin-top:0 white-space: nowrap'><font color='" + getLevelColor(level) + "'>" + finalMsg + "</font></p>"

            htmlLog += htmlLine

            console?.update()

            msg += "\n"
            writeToFile(msg.toString())
        }

        fun info(message: String) = log(message, CCLogLevel.INFO)
        fun warn(message: String) = log(message, CCLogLevel.WARNING)
        fun error(message: String) = log(message, CCLogLevel.ERROR)
        fun debug(message: String) = log(message, CCLogLevel.ERROR)

        fun log(message: String, level: CCLogLevel) {
            if(!enabled) return

            if(paused) {
                pausedMessages.add(CCLogMessage(level, message, LocalDateTime.now(), getStackTrace(), false))
                return
            }

            logInternal(message, level, LocalDateTime.now(), getStackTrace())
        }

        private fun writeToFile(message: String) {
            if(logFile != null) {
                try {
                    Files.write(Paths.get(logFile!!.absolutePath), message.toByteArray(), StandardOpenOption.APPEND)
                } catch (ioException: IOException) {
                    val path = logFile!!.absolutePath
                    logFile = null
                    log("Could not write to logfile at \"$path\". Disabling logFile & Printing to console as well just in case!", CCLogLevel.ERROR)
                    logStacktrace(ioException, CCLogLevel.ERROR)
                }
            } else {
                preFileMessages.add(message)
            }
        }

        fun logStacktrace(message: String, level: CCLogLevel) {
            if(!enabled) return

            val stackTrace = Thread.currentThread().stackTrace
            val STACKTRACE_START = 2

            StringBuilder().also { sb ->
                for(i in STACKTRACE_START until stackTrace.size) {
                    val trace = stackTrace[i].toString()
                    //TODO: This should not be hardcoded
                    //if(trace.contains("net.snipsniper"))
                        sb.append(trace).append("\n")
                }
                logStacktraceInternal(sb.toString(), level)
            }
        }

        fun logStacktrace(exception: Exception, level: CCLogLevel) {
            if(!enabled) return
            logStacktraceInternal(ExceptionUtils.getStackTrace(exception), level)
        }

        private fun getLevelColor(level: CCLogLevel): String {
            var color = "white"
            if(level == CCLogLevel.WARNING)
                color = "yellow"
            else if(level == CCLogLevel.ERROR)
                color = "red"
            return color
        }

        private fun refreshPreFileMessages() {
            preFileMessages.forEach {
                writeToFile(it)
            }
            preFileMessages.clear()
        }

        private fun getStackTrace(): StackTraceElement {
            //We loop through the stacktrace till we get to whoever called the logger. We skip the first element as its java.lang.Thread
            val stackTrace = Thread.currentThread().stackTrace
            var startIndex = 1
            for(i in 1 until stackTrace.size) {
                if(stackTrace[startIndex].className.startsWith(THIS_CLASSPATH))
                    startIndex++
                else
                    break
            }
            return stackTrace[startIndex]
        }

        private fun logStacktraceInternal(message: String, level: CCLogLevel) {
            if(!enabled)
                return

            if(paused) {
                pausedMessages.add(CCLogMessage(level, message, LocalDateTime.now(), null, true))
                return
            }

            println(message)
            writeToFile(message)
            htmlLog += "<p style='margin-top:0 white-space: nowrap'><font color='" + getLevelColor(level) + "'>" + StringEscapeUtils.escapeHtml4(message).replace("\n", "<br>") + "</font></p>"
            htmlLog += "<br>"
        }

        fun setPaused(paused: Boolean) {
            this.paused = paused
            if(!paused) {
                pausedMessages.forEach { msg ->
                    if(!msg.isException) {
                        logInternal(msg.message, msg.level, msg.time, msg.stackTraceElement ?: throw Exception("Oops! CCLogger 169 issue, stackTraceElement is null!!"))
                    } else {
                        logStacktraceInternal(msg.message, msg.level)
                    }
                }
                pausedMessages.clear()
            }
        }

        fun enableDebugConsole(enabled: Boolean) {
            if(enabled) {
                if(console == null)
                    console = CCDebugConsole()
                console?.update()
            } else {
                console?.isVisible = false
                console?.dispose()
                console = null
            }
        }
    }
}