package org.capturecoop.cclogger;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class CCLogger {
    private static File logFile;
    private static final int MAX_LEVEL_LENGTH = LogLevel.WARNING.toString().length();
    private static boolean enabled = false;
    private static final ArrayList<LogMessage> preEnabledMessages = new ArrayList<>();
    private static String logFormat = "";
    private static String htmlLog = "";

    private CCLogger() {}

    public static void log(String message, LogLevel level) {
        if(!enabled) {
            preEnabledMessages.add(new LogMessage(level, message, LocalDateTime.now()));
            return;
        }
        logInternal(message, level, LocalDateTime.now());
    }

    public static void log(String message, LogLevel level, Object... args) {
        log(org.capturecoop.ccutils.utils.StringUtils.format(message, args), level);
    }

    public static void logStacktrace(LogLevel level) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        final int STACKTRACE_START = 2;

        StringBuilder stackTraceString = new StringBuilder();
        for(int i = STACKTRACE_START; i < stackTrace.length; i++) {
            String trace = stackTrace[i].toString();
            if(trace.contains("net.snipsniper"))
                stackTraceString.append(trace).append("\n");
        }
        logStacktraceInternal(stackTraceString.toString(), level);
    }

    public static void logStacktrace(Throwable exception, LogLevel level) {
        logStacktraceInternal(ExceptionUtils.getStackTrace(exception), level);
    }

    private static void logStacktraceInternal(String message, LogLevel level) {
        System.out.println(message);
        htmlLog += "<p style='margin-top:0; white-space: nowrap;'><font color='" + getLevelColor(level) + "'>" + org.apache.commons.text.StringEscapeUtils.escapeHtml4(message).replaceAll("\n", "<br>") + "</font></p>";
        htmlLog += "<br>";
    }

    //The reason for this is that this way we can take index 3 of stack trace at all times
    private static void logInternal(String message, LogLevel level, LocalDateTime time) {
        if(!enabled)
            return;

        if(level == LogLevel.DEBUG && !SnipSniper.isDebug())
            return;

        StringBuilder msg = new StringBuilder(logFormat);
        msg = new StringBuilder(org.capturecoop.ccutils.utils.StringUtils.formatDateTimeString(msg.toString(), time));

        String levelString = level.toString();

        if(levelString.length() <= MAX_LEVEL_LENGTH) {
            msg = new StringBuilder(msg.toString().replace("%levelspace%", StringUtils.repeat(" ", MAX_LEVEL_LENGTH - levelString.length())));
        } else {
            levelString = levelString.substring(0, MAX_LEVEL_LENGTH);
            msg = new StringBuilder(msg.toString().replace("%levelspace%", ""));
        }

        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        final int STACKTRACE_START = 3;
        StackTraceElement currentStackTrace = stackTrace[STACKTRACE_START];

        String classFilename = currentStackTrace.getFileName();
        if(classFilename != null)
            classFilename = classFilename.replaceAll(".java" ,"");

        msg = new StringBuilder(msg.toString().replace("%filename%", classFilename));
        msg = new StringBuilder(msg.toString().replace("%method%", currentStackTrace.getMethodName()));
        msg = new StringBuilder(msg.toString().replace("%line%", currentStackTrace.getLineNumber() + ""));

        msg = new StringBuilder(msg.toString().replace("%levelspace%", ""));
        msg = new StringBuilder(msg.toString().replace("%level%", levelString));
        msg = new StringBuilder(msg.toString().replace("%message%", message));

        System.out.println(msg.toString().replaceAll("%newline%", "\n"));

        String finalMsg = org.apache.commons.text.StringEscapeUtils.escapeHtml4(msg.toString()).replaceAll(" ", "&nbsp;");
        finalMsg = finalMsg.replaceAll("%newline%", "<br>");
        if(SnipSniper.getConfig() != null) {
            String baseTreeLink = "https://github.com/CaptureCoop/SnipSniper/tree/" + SnipSniper.getVersion().getGithash() + "/src/main/java/";
            String link = baseTreeLink + currentStackTrace.getClassName().replaceAll("\\.", "/") + ".java#L" + currentStackTrace.getLineNumber();
            finalMsg = finalMsg.replace(":" + currentStackTrace.getLineNumber() + "]", ":" + currentStackTrace.getLineNumber() + " <a href='" + link + "'>@</a>]");
        }
        String htmlLine = "<p style='margin-top:0; white-space: nowrap;'><font color='" + getLevelColor(level) + "'>" + finalMsg + "</font></p>";

        htmlLog += htmlLine;

        DebugConsole console = SnipSniper.getDebugConsole();
        if(console != null)
            console.update();

        if(!SnipSniper.isDemo() && SnipSniper.getLogFolder() != null) {
            if (logFile == null) {
                LocalDateTime now = LocalDateTime.now();
                String filename = now.toString().replace(".", "_").replace(":", "_");
                filename += ".log";

                logFile = new File(SnipSniper.getLogFolder() + filename);
                try {
                    if (logFile.createNewFile())
                        CCLogger.log("Created new logfile at: " + logFile.getAbsolutePath(), LogLevel.INFO);
                } catch (IOException ioException) {
                    CCLogger.log("Could not create logfile at \"%c\". Printing to console as well just in case!", LogLevel.ERROR, logFile.getAbsolutePath());
                    CCLogger.logStacktrace(ioException, LogLevel.ERROR);
                    ioException.printStackTrace();
                }
            }

            msg.append("\n");

            try {
                Files.write(Paths.get(logFile.getAbsolutePath()), msg.toString().getBytes(), StandardOpenOption.APPEND);
            } catch (IOException ioException) {
                CCLogger.log("Could not create logfile at \"%c\". Printing to console as well just in case!", LogLevel.ERROR, logFile.getAbsolutePath());
                CCLogger.logStacktrace(ioException, LogLevel.ERROR);
                ioException.printStackTrace();
            }
        }
    }

    public static String getLevelColor(LogLevel level) {
        String color = "white";
        if(level == LogLevel.WARNING)
            color = "yellow";
        else if(level == LogLevel.ERROR)
            color = "red";
        return color;
    }

    public static void setEnabled(boolean enabled) {
        CCLogger.enabled = enabled;
        if(enabled) {
            for(LogMessage msg : preEnabledMessages)
                logInternal(msg.getMessage(), msg.getLevel(), msg.getTime());
            preEnabledMessages.clear();
        }
    }

    public static File getLogFile() {
        return logFile;
    }

}
