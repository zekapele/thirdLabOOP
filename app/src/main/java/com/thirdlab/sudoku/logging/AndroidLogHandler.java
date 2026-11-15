package com.thirdlab.sudoku.logging;

import android.util.Log;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

public final class AndroidLogHandler extends Handler {
    private static final String TAG = "Sudoku";

    public AndroidLogHandler() {
        setLevel(Level.FINE);
        setFormatter(new SimpleFormatter());
    }

    @Override
    public synchronized void publish(LogRecord record) {
        if (!isLoggable(record)) {
            return;
        }
        String loggerName = record.getLoggerName() == null ? TAG : record.getLoggerName();
        int dot = loggerName.lastIndexOf('.');
        String source = dot >= 0 ? loggerName.substring(dot + 1) : loggerName;
        String message = getFormatter().formatMessage(record);
        if (message == null) {
            message = "";
        }
        String text = source + ": " + message;
        int priority = priority(record.getLevel());
        if (record.getThrown() != null) {
            Log.println(priority, TAG, text + "\n" + Log.getStackTraceString(record.getThrown()));
            return;
        }
        Log.println(priority, TAG, text);
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() {
    }

    private static int priority(Level level) {
        int value = level.intValue();
        if (value >= Level.SEVERE.intValue()) {
            return Log.ERROR;
        }
        if (value >= Level.WARNING.intValue()) {
            return Log.WARN;
        }
        if (value >= Level.INFO.intValue()) {
            return Log.INFO;
        }
        return Log.DEBUG;
    }
}
