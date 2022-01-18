package org.capturecoop.cclogger;

import java.time.LocalDateTime;

public class LogMessage {
    private final LogLevel level;
    private final String message;
    private final LocalDateTime time;
    private boolean isStacktrace;

    public LogMessage(LogLevel level, String message, LocalDateTime time, boolean isException) {
        this.level = level;
        this.message = message;
        this.time = time;
        this.isStacktrace = isException;
    }

    public LogLevel getLevel() {
        return level;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public boolean isStacktrace() {
        return isStacktrace;
    }
}
