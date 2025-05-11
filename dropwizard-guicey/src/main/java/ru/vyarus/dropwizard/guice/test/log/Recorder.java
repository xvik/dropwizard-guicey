package ru.vyarus.dropwizard.guice.test.log;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.filter.ThresholdFilter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Logs recorder. Applies custom appender for logback logger(s) and change level, if required. Collect all
 * log events.
 *
 * @author Vyacheslav Rusakov
 * @since 26.02.2025
 */
public class Recorder {
    private final ListAppender<ILoggingEvent> appender = new ListAppender<>();
    private final List<String> loggers;
    private final Level level;

    /**
     * Create recorder.
     *
     * @param name    name
     * @param level   base level
     * @param loggers target loggers
     */
    public Recorder(final String name, final org.slf4j.event.Level level, final List<String> loggers) {
        this.loggers = loggers;
        this.level = Level.toLevel(level.toString());
        // custom name (field name) to clearly see custom appender
        appender.setName(name);

        final ThresholdFilter levelFilter = new ThresholdFilter();
        levelFilter.setLevel(level.toString());
        levelFilter.start();

        appender.addFilter(levelFilter);
    }

    /**
     * @return raw recorded records
     * @see #getRecordedLogs() for easy navigation
     */
    public List<ILoggingEvent> getRecords() {
        return appender.list;
    }

    /**
     * Note that object always returns actual logs. So the same instance could be used to all verifications.
     *
     * @return recorded logs navigation object
     */
    public RecordedLogs getRecordedLogs() {
        return new RecordedLogs(this);
    }

    /**
     * Clear recordings.
     */
    public void clear() {
        appender.list.clear();
    }

    /**
     * Could be called multiple times. Initially called before application start to record all startup events.
     * But dropwizard reset loggers just before run phase, so appender must be applied second time.
     * <p>
     * DON'T call this method manually: simply no need - hook will call it in appropriate moments.
     */
    public void attach() {
        final boolean rootLevelMatch = isRootLevelMatch();
        getMatchedLoggers().forEach(logger -> {
            if (!logger.isAttached(appender)) {
                logger.addAppender(appender);
            }
            // lower logger level to receive all required events
            // if logger does not have level - see root logger (root loger threshold might be lower than required
            // so it would be not correct to set level at any case)
            if ((logger.getLevel() != null && !isLevelMatch(logger)) || !rootLevelMatch) {
                logger.setLevel(level);
            }
        });
        appender.start();
    }

    /**
     * Remove appender from logger.
     * <p>
     * Not required as dropwizard reset all logging during application startup
     * and so stale appenders would be removed in any case before each new test.
     */
    public void destroy() {
        appender.stop();
        getMatchedLoggers().forEach(logger -> logger.detachAppender(appender));
    }

    private boolean isRootLevelMatch() {
        // check if the root logger level is lower than required
        return isLevelMatch((Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME));
    }

    private boolean isLevelMatch(final Logger logger) {
        final Level currentLevel = logger.getLevel();
        // if level not set look root logger level to match (it will define the logged threshold)
        return currentLevel != null && (currentLevel.equals(level) || !currentLevel.isGreaterOrEqual(level));
    }

    private List<Logger> getMatchedLoggers() {
        final List<Logger> res = new ArrayList<>();
        if (loggers.isEmpty()) {
            res.add((Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME));
        } else {
            loggers.forEach(loggerName -> res.add((Logger) LoggerFactory.getLogger(loggerName)));
        }
        return res;
    }
}
