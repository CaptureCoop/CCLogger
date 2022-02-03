# CCLogger
Logging library

## Debug Console

This library includes a Debug Console that can optionally be used.
However without a Look and feel this may look bad and zooming may not work.
We personally use FlatLaf, which works great when used with the Debug Console.

## How to use with Gradle:

build.gradle:
```
dependencies {
    implementation 'org.capturecoop:CCLogger:VERSION'
}
```
settings.gradle:
```
sourceControl {
    gitRepository("https://github.com/CaptureCoop/CCLogger.git") {
        producesModule("org.capturecoop:CCLogger")
    }
}
```

## Setup in code

Example settings:

```
CCLogger.setEnabled(true);
CCLogger.setPaused(true);
CCLogger.setLogFormat("[%hour%:%minute%:%second%:%ms%] [%level%]%levelspace% [%filename%.%method%:%line%]: %message%"));
CCLogger.setLogFile(new File("file.log"));
CCLogger.setGitHubCodePathURL("https://github.com/CaptureCoop/SnipSniper/tree/<githash>/src/main/java/");
CCLogger.setGitHubCodeClassPath("net.snipsniper");
CCLogger.setPaused(false);
```

TLDR:

setEnabled is required for the logger to do anything. setPaused allows you to store logged messages till you unpause it, which is useful for setting up parameters.

The GitHub code setters are for the DebugConsole to allow you clicking on classes to open them in your browser. The GitHubCodeClassPath is a whitelist to only add the @ link to said package.

You can leave that away and the @ will be added to every class.

## Dependencies

* [Apache Commons Text](https://mvnrepository.com/artifact/org.apache.commons/commons-text)
* [Apache Commons Lang](https://mvnrepository.com/artifact/org.apache.commons/commons-lang)
* [CCUtils](https://github.com/capturecoop/ccutils)

## License

MIT License

Copyright (c) 2022 CaptureCoop.org

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
