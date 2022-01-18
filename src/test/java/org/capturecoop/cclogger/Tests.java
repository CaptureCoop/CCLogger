package org.capturecoop.cclogger;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

public class Tests {
    @Test
    public void test() throws InterruptedException {
        CCLogger.log("Hello world!", LogLevel.INFO);
        CCLogger.log("There is no log file yet! Setting one now...", LogLevel.INFO);
        CCLogger.log("Logging a stacktrace", LogLevel.WARNING);
        gaga();
        CCLogger.setLogFile(new File("testlog.log"));
        CCLogger.log("Thats done!", LogLevel.INFO);
        CCLogger.log("Now we will enable the debug console!", LogLevel.INFO);
        CCLogger.enableDebugConsole(true);
        Thread.sleep(5000);
        CCLogger.log("Great! Disabling now", LogLevel.ERROR);
        CCLogger.enableDebugConsole(false);
        CCLogger.log("Waiting 2 seconds and enabling again...", LogLevel.ERROR);
        Thread.sleep(2000);
        CCLogger.log("Psssht. Secret debug message!", LogLevel.DEBUG);
        CCLogger.enableDebugConsole(true);
        Thread.sleep(5000);
    }

    public void gaga() {
        gugu();
    }

    public void gugu() {
        CCLogger.logStacktrace(new IOException("test"), LogLevel.WARNING);
    }
}
