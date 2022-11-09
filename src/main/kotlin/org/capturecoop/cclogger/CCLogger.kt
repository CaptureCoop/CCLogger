package org.capturecoop.cclogger

import java.awt.Color
import org.apache.commons.lang3.exception.ExceptionUtils
import org.apache.commons.text.StringEscapeUtils
import org.capturecoop.ccutils.utils.CCStringUtils
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.time.LocalDateTime
import javax.imageio.ImageIO

class CCLogger {
    companion object {
        val icon = ImageIO.read(CCDebugConsole::class.java.getResource("/org/capturecoop/cclogger/resources/console.png")) ?: throw FileNotFoundException("console.png not found!")
        var enabled = false
        var paused = false
            set(value) {
                field = value
                updatePaused()
            }
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

        var filter = CCLogFilter.INFO

        //The reason for this is that this way we can take index 3 of stack trace at all times
        private fun logInternal(message: String, level: CCLogLevel, time: LocalDateTime, currentStackTrace: StackTraceElement) {
            if(!enabled || filter == CCLogFilter.NONE) return
            when(filter) {
                CCLogFilter.ERRORS -> if(level.isNot(CCLogLevel.ERROR)) return
                CCLogFilter.WARNINGS -> if(level.isNot(CCLogLevel.ERROR, CCLogLevel.WARNING)) return
                CCLogFilter.INFO -> if(level.isNot(CCLogLevel.ERROR, CCLogLevel.WARNING, CCLogLevel.INFO)) return
                else -> {}
            }

            var msg = CCStringUtils.formatDateTimeString(logFormat, time)

            //Handle %level% and %levelspace%
            level.toString().also {
                var levelString = it
                if(levelString.length <= MAX_LEVEL_LENGTH) {
                    msg = msg.replace("%levelspace%", " ".repeat(MAX_LEVEL_LENGTH - levelString.length))
                } else {
                    levelString = levelString.substring(0, MAX_LEVEL_LENGTH)
                    msg = msg.replace("%levelspace%", "")
                }
                msg = msg.replace("%level%", levelString)
            }

            //Handle %filename% and %fileext%
            File(currentStackTrace.fileName ?: "null").also {
                msg = msg.replace("%filename%", it.nameWithoutExtension)
                msg = msg.replace("%fileext%", it.extension)
            }

            //Handle %method%, %line%, %message% and %newline%
            msg = msg.replace("%method%", currentStackTrace.methodName)
            msg = msg.replace("%line%", currentStackTrace.lineNumber.toString())
            msg = msg.replace("%message%", message)
            msg = msg.replace("%newline%", "\n")

            println(level.getAnsiColor() + msg + level.getAnsiReset())

            var finalMsg = StringEscapeUtils.escapeHtml4(msg.toString()).replace(" ", "&nbsp;")
            finalMsg = finalMsg.replace("%newline%", "<br>")
            if(gitHubCodePathURL != null && (gitHubCodeClassPath == null || gitHubCodeClassPath.isNullOrEmpty() || currentStackTrace.className.contains(gitHubCodeClassPath!!))) {
                val link = gitHubCodePathURL + currentStackTrace.className.replace("\\.", "/") + ".java#L" + currentStackTrace.lineNumber
                finalMsg = finalMsg.replace(":" + currentStackTrace.lineNumber + "]", ":" + currentStackTrace.lineNumber + " <a href='" + link + "'>@</a>]")
            }
            val htmlLine = "<p style='margin-top:0 white-space: nowrap'><font color='${level.getHTMLColor()}'>${finalMsg}</font></p>"

            htmlLog += htmlLine

            console?.update()

            msg += "\n"
            writeToFile(msg.toString())
        }

        fun info(message: Any?) = log(message, CCLogLevel.INFO)
        fun warn(message: Any?) = log(message, CCLogLevel.WARNING)
        fun error(message: Any?) = log(message, CCLogLevel.ERROR)
        fun debug(message: Any?) = log(message, CCLogLevel.DEBUG)

        fun log(message: Any?, level: CCLogLevel) {
            if(!enabled) return

            if(paused) {
                pausedMessages.add(CCLogMessage(level, message.toString(), LocalDateTime.now(), getStackTrace(), false))
                return
            }

            logInternal(message.toString(), level, LocalDateTime.now(), getStackTrace())
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
                    //TODO: Is this ok? Should we limit?
                    sb.append("$trace\n")
                }
                logStacktraceInternal(sb.toString(), level)
            }
        }

        fun logStacktrace(exception: Exception, level: CCLogLevel) {
            if(!enabled) return
            logStacktraceInternal(ExceptionUtils.getStackTrace(exception), level)
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
            val htmlEscaped = StringEscapeUtils.escapeHtml4(message).replace("\n", "<br>")
            htmlLog += "<p style='margin-top:0 white-space: nowrap'><font color='${level.getHTMLColor()}'>${htmlEscaped}</font></p>"
            htmlLog += "<br>"
        }

        private fun updatePaused() {
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