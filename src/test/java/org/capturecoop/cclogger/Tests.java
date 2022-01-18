package org.capturecoop.cclogger;

import org.junit.jupiter.api.Test;

import java.io.File;

public class Tests {
    @Test
    public void test() {
        CCLogger.log("Hello world!", LogLevel.INFO);
        CCLogger.log("There is no log file yet! Setting one now...", LogLevel.INFO);
        CCLogger.setLogFile(new File("testlog.log"));
        CCLogger.log("Thats done!", LogLevel.INFO);
    }
}
