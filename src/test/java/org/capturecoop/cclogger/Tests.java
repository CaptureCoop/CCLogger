package org.capturecoop.cclogger;

import org.junit.jupiter.api.Test;

import java.io.File;

public class Tests {
    @Test
    public void test() throws InterruptedException {
        CCLogger.setEnabled(true);
        CCLogger.log("Hello world!", LogLevel.INFO);
        CCLogger.log("There is no log file yet! Setting one now...", LogLevel.INFO);
        CCLogger.setLogFile(new File("testlog.log"));
        CCLogger.log("Thats done!", LogLevel.INFO);
        CCLogger.log("Now we will enable the debug console!", LogLevel.INFO);
        CCLogger.enableDebugConsole(true);
        CCLogger.log("Test", LogLevel.INFO);
        Thread.sleep(1000);
        CCLogger.log("BLABLA", LogLevel.INFO);
        for(int i = 0; i < 40; i++) {
            CCLogger.log("A: " + i, LogLevel.INFO);
            Thread.sleep(100);
        }
        Thread.sleep(10000);
    }
}
