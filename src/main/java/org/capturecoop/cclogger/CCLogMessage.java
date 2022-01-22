package org.capturecoop.cclogger;

import java.time.LocalDateTime;

public class CCLogMessage {
    private final CCLogLevel level;
    private final String message;
    private final LocalDateTime time;
    private final StackTraceElement stackTraceElement;
    private final boolean isStacktrace;

    public CCLogMessage(CCLogLevel level, String message, LocalDateTime time, StackTraceElement stackTraceElement, boolean isException) {
        this.level = level;
        this.message = message;
        this.time = time;
        this.stackTraceElement = stackTraceElement;
        this.isStacktrace = isException;
    }

    public CCLogLevel getLevel() {
        return level;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public StackTraceElement getStackTraceElement() {
        return stackTraceElement;
    }

    public boolean isStacktrace() {
        return isStacktrace;
    }
}
