package org.capturecoop.cclogger;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.capturecoop.ccutils.utils.CCStringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class CCLogger {
    private static boolean enabled = false;
    private static boolean paused = false;
    private static File logFile;
    private static final int MAX_LEVEL_LENGTH = CCLogLevel.WARNING.toString().length();
    private static final ArrayList<String> preFileMessages = new ArrayList<>(); //These are printed but not written right away
    private static final ArrayList<CCLogMessage> pausedMessages = new ArrayList<>(); //These need to be constructed once ready
    private static String logFormat = "[%hour%:%minute%:%second%:%ms%] [%level%]%levelspace% [%filename%.%method%:%line%]: %message%";
    private static String htmlLog = "";
    private static String gitHubCodePathURL = null; //Example: https://github.com/CaptureCoop/SnipSniper/tree/<HASH HERE>/src/main/java/"
    private static String gitHubCodeClassPath; //Example: org.snipsniper -> If set only messages that contain this classpath get the @ link
    private static CCDebugConsole console;
    private static final String THIS_CLASSPATH = "org.capturecoop.cclogger.CCLogger";

    // Debug    -> Log everything + debug messages
    // Info     -> Log everything
    // Warning  -> Log warnings and errors
    // Error    -> Log errors
    private static CCLogLevel logLevel = CCLogLevel.INFO;

    private CCLogger() {}

    public static void log(String message) {
        log(message, CCLogLevel.INFO);
    }

    public static void log(String message, CCLogLevel level) {
        if(!enabled)
            return;

        if(paused) {
            pausedMessages.add(new CCLogMessage(level, message, LocalDateTime.now(), getStackTrace(), false));
            return;
        }

        logInternal(message, level, LocalDateTime.now(), getStackTrace());
    }

    public static void log(String message, CCLogLevel level, Object... args) {
        if(!enabled)
            return;

        log(org.capturecoop.ccutils.utils.CCStringUtils.format(message, args), level);
    }

    public static void logStacktrace(CCLogLevel level) {
        if(!enabled)
            return;

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

    public static void logStacktrace(Throwable exception, CCLogLevel level) {
        if(!enabled)
            return;

        logStacktraceInternal(ExceptionUtils.getStackTrace(exception), level);
    }

    private static void logStacktraceInternal(String message, CCLogLevel level) {
        if(!enabled)
            return;

        if(paused) {
            pausedMessages.add(new CCLogMessage(level, message, LocalDateTime.now(), null, true));
            return;
        }

        System.out.println(message);
        writeToFile(message);
        htmlLog += "<p style='margin-top:0; white-space: nowrap;'><font color='" + getLevelColor(level) + "'>" + org.apache.commons.text.StringEscapeUtils.escapeHtml4(message).replaceAll("\n", "<br>") + "</font></p>";
        htmlLog += "<br>";
    }

    private static StackTraceElement getStackTrace() {
        //We loop through the stacktrace till we get to whoever called the logger. We skip the first element as its java.lang.Thread
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        int startIndex;
        for(startIndex = 1; startIndex < stackTrace.length; startIndex++) {
            if(stackTrace[startIndex].getClassName().startsWith(THIS_CLASSPATH))
                startIndex++;
            else
                break;
        }
        return stackTrace[startIndex];
    }

    //The reason for this is that this way we can take index 3 of stack trace at all times
    private static void logInternal(String message, CCLogLevel level, LocalDateTime time, StackTraceElement currentStackTrace) {
        if(!enabled)
            return;

        if(level == CCLogLevel.DEBUG && logLevel == CCLogLevel.DEBUG)
            return;

        StringBuilder msg = new StringBuilder(logFormat);
        msg = new StringBuilder(CCStringUtils.formatDateTimeString(msg.toString(), time));

        String levelString = level.toString();

        if(levelString.length() <= MAX_LEVEL_LENGTH) {
            msg = new StringBuilder(msg.toString().replace("%levelspace%", StringUtils.repeat(" ", MAX_LEVEL_LENGTH - levelString.length())));
        } else {
            levelString = levelString.substring(0, MAX_LEVEL_LENGTH);
            msg = new StringBuilder(msg.toString().replace("%levelspace%", ""));
        }

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
        if(gitHubCodePathURL != null && (gitHubCodeClassPath == null || gitHubCodeClassPath.isEmpty() || currentStackTrace.getClassName().contains(gitHubCodeClassPath))) {
            String link = gitHubCodePathURL + currentStackTrace.getClassName().replaceAll("\\.", "/") + ".java#L" + currentStackTrace.getLineNumber();
            finalMsg = finalMsg.replace(":" + currentStackTrace.getLineNumber() + "]", ":" + currentStackTrace.getLineNumber() + " <a href='" + link + "'>@</a>]");
        }
        String htmlLine = "<p style='margin-top:0; white-space: nowrap;'><font color='" + getLevelColor(level) + "'>" + finalMsg + "</font></p>";

        htmlLog += htmlLine;

        if(console != null)
            console.update();

        msg.append("\n");
        writeToFile(msg.toString());
    }

    public static void writeToFile(String message) {
        if(logFile != null) {
            try {
                Files.write(Paths.get(logFile.getAbsolutePath()), message.getBytes(), StandardOpenOption.APPEND);
            } catch (IOException ioException) {
                String path = logFile.getAbsolutePath();
                logFile = null;
                CCLogger.log("Could not write to logfile at \"%c\". Disabling logFile & Printing to console as well just in case!", CCLogLevel.ERROR, path);
                CCLogger.logStacktrace(ioException, CCLogLevel.ERROR);
            }
        } else {
            preFileMessages.add(message);
        }
    }

    public static String getLevelColor(CCLogLevel level) {
        String color = "white";
        if(level == CCLogLevel.WARNING)
            color = "yellow";
        else if(level == CCLogLevel.ERROR)
            color = "red";
        return color;
    }

    public static String getHTMLLog() {
        return htmlLog;
    }

    public static void setLogFile(File logFile) {
        CCLogger.logFile = logFile;
        if(!logFile.exists()) {
            try {
                if (logFile.createNewFile()) {
                    CCLogger.log("Set log file does not exist. Creating: %c", CCLogLevel.INFO, logFile.getAbsolutePath());
                }
            } catch (IOException ioException) {
                CCLogger.log("Set log file does not exist and could not be created. File: %c", CCLogLevel.ERROR, logFile.getAbsolutePath());
                CCLogger.logStacktrace(ioException, CCLogLevel.ERROR);
            }
        }
        refreshPreFileMessages();
    }

    public static void enableDebugConsole(boolean enabled) {
        if(enabled) {
            if(console == null)
                console = new CCDebugConsole();
            console.update();
        } else {
            console.setVisible(false);
            console.dispose();
            console = null;
        }
    }

    private static void refreshPreFileMessages() {
        if(!preFileMessages.isEmpty()) {
            for(String m : preFileMessages) {
                writeToFile(m);
            }
            preFileMessages.clear();
        }
    }

    public static void setPaused(boolean paused) {
        CCLogger.paused = paused;
        if(!paused) {
            for(CCLogMessage msg : pausedMessages) {
                if(!msg.isStacktrace()) {
                    logInternal(msg.getMessage(), msg.getLevel(), msg.getTime(), msg.getStackTraceElement());
                } else {
                    logStacktraceInternal(msg.getMessage(), msg.getLevel());
                }
            }
            pausedMessages.clear();
        }
    }

    public static void setEnabled(boolean enabled) {
        CCLogger.enabled = enabled;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static File getLogFile() {
        return logFile;
    }

    public static void setGitHubCodePathURL(String url) {
        gitHubCodePathURL = url;
    }

    public static void setGitHubCodeClassPath(String classPath) {
        gitHubCodeClassPath = classPath;
    }

    public static void setLogFormat(String logFormat) {
        CCLogger.logFormat = logFormat;
    }

    public static void setLogLevel(CCLogLevel logLevel) {
        CCLogger.logLevel = logLevel;
    }

}
