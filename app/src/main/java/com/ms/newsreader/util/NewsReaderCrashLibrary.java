package com.ms.newsreader.util;

/**
 * Created by Mohd. Shariq on 22/10/16.
 */

public class NewsReaderCrashLibrary {
    public static void log(int priority, String tag, String message) {
        // TODO add log entry to circular buffer.
    }

    public static void logWarning(Throwable t) {
        // TODO report non-fatal warning.
    }

    public static void logError(Throwable t) {
        // TODO report non-fatal error.
    }

    private NewsReaderCrashLibrary() {
        throw new AssertionError("No instances.");
    }

}
