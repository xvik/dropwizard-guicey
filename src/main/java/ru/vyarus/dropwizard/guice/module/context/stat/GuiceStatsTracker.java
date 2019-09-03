package ru.vyarus.dropwizard.guice.module.context.stat;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Guice logs internal stats with java util logger ({@link com.google.inject.internal.util.Stopwatch}). In order to
 * intercept these messages, append custom handler to this logger, but just for injector creation time.
 */
public class GuiceStatsTracker {

    private final List<String> messages = new ArrayList<>();
    private Level originalLevel;

    private final Handler handler = new LogsInterceptor();

    /**
     * Cut off guice logger from parent handlers and add individual handler to intercept log messages.
     */
    public void injectLogsInterceptor() {
        final Logger guiceStatsLogger = getLogger();
        // cut off logger only if its level is not enough to intercept messages
        // otherwise means user configure logging to see these logs
        if (!guiceStatsLogger.isLoggable(Level.FINE)) {
            originalLevel = guiceStatsLogger.getLevel();
            guiceStatsLogger.setUseParentHandlers(false);
            guiceStatsLogger.setLevel(Level.FINE);
        }
        guiceStatsLogger.addHandler(handler);
    }

    /**
     * After injector creation reset logger back to parent handler.
     */
    public void resetStatsLogger() {
        final Logger guiceStatsLogger = getLogger();
        guiceStatsLogger.removeHandler(handler);
        guiceStatsLogger.setUseParentHandlers(true);
        if (originalLevel != null) {
            guiceStatsLogger.setLevel(originalLevel);
            originalLevel = null;
        }
    }

    /**
     * @return guice injector creation stat messages with non zero time
     */
    public List<String> getMessages() {
        return messages;
    }

    private Logger getLogger() {
        return Logger.getLogger(com.google.inject.internal.util.Stopwatch.class.getName());
    }

    /**
     * Intercept guice stats logs.
     */
    private class LogsInterceptor extends Handler {
        @Override
        public void publish(final LogRecord record) {
            final String msg = record.getMessage();
            // add space between number and ms
            messages.add(msg.substring(0, msg.length() - 2) + " ms");
        }

        @Override
        public void flush() {
            // nothing
        }

        @Override
        public void close() throws SecurityException {
            // nothing
        }
    }
}
