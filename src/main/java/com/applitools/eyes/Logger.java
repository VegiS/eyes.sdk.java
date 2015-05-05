package com.applitools.eyes;

import com.applitools.utils.ArgumentGuard;

/**
 * Logs trace messages.
 */
public class Logger {
    private LogHandler logHandler;

    public Logger() {
        logHandler = new NullLogHandler(); // Default.
    }

    /**
     * @return The currently set log handler.
     */
    public LogHandler getLogHandler() {
        return logHandler;
    }

    /**
     * Sets the log handler.
     * @param handler The log handler to set. If you want a log handler which
     *                does nothing, use {@link
     *                com.applitools.eyes.NullLogHandler}.
     */
    public void setLogHandler(LogHandler handler) {
        ArgumentGuard.notNull(handler, "handler");

        logHandler = handler;
    }

    /**
     * Writes a verbose write message.
     * @param message The message to log as verbose.
     */
    public void verbose(String message) {
        logHandler.onMessage(true, message);
    }

    /**
     * Writes a (non-verbose) write message.
     * @param message The message to log.
     */
    public void log(String message) {
        logHandler.onMessage(false, message);
    }
}