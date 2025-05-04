package ru.vyarus.dropwizard.guice.test.log;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Log records selector object. To avoid tons of selection methods with different parameters, all selection methods
 * return sub-selector object for further selections. At any selection step events could be obtained with
 * {@link #events()} or {@link #messages()}.
 *
 * @author Vyacheslav Rusakov
 * @since 28.02.2025
 */
public class LogsSelector {

    protected final List<ILoggingEvent> list;

    public LogsSelector(final List<ILoggingEvent> list) {
        this.list = list;
    }

    /**
     * @return true when no logs recorded
     */
    public boolean empty() {
        return count() == 0;
    }

    /**
     * @return count of log records
     */
    public int count() {
        return list.size();
    }

    /**
     * @return list of raw events
     */
    public List<ILoggingEvent> events() {
        return list;
    }

    /**
     * @return formatted messages (without logger class, raw message)
     */
    public List<String> messages() {
        return messages(ILoggingEvent::getFormattedMessage);
    }

    /**
     * @param mapper mapper function
     * @return messages after custom formatter
     */
    public List<String> messages(final Function<ILoggingEvent, String> mapper) {
        return list.stream().map(mapper).collect(Collectors.toList());
    }

    /**
     * @param loggerName logger name
     * @return true if logged messages found for required logger
     */
    public boolean has(final String loggerName) {
        return list.stream().anyMatch(event -> event.getLoggerName().equals(loggerName));
    }

    /**
     * @param logger logger class
     * @return true if logged messages found for required logger
     */
    public boolean has(final Class<?> logger) {
        return has(logger.getName());
    }

    /**
     * @param level required level
     * @return true if logged messages found for required level
     */
    public boolean has(final org.slf4j.event.Level level) {
        // toString not error - it returns string level representation
        final Level reqLevel = Level.valueOf(level.toString());
        return list.stream().anyMatch(event -> event.getLevel().equals(reqLevel));
    }

    /**
     * Generic event selector.
     *
     * @param predicate selection predicate
     * @return selector for selected events
     */
    public LogsSelector select(final Predicate<ILoggingEvent> predicate) {
        return new LogsSelector(list.stream().filter(predicate).collect(Collectors.toList()));
    }

    /**
     * @param levels required levels
     * @return sub selector with filtered logs from other levels
     */
    public LogsSelector level(final org.slf4j.event.Level... levels) {
        // toString not error - it returns string level representation
        final List<Level> reqLevel = Arrays.stream(levels)
                .map(it -> Level.valueOf(it.toString())).collect(Collectors.toList());
        return select(event -> reqLevel.contains(event.getLevel()));
    }

    /**
     * @param loggerNames logger name
     * @return sub selector with filtered logs from other loggers
     */
    public LogsSelector logger(final String... loggerNames) {
        final List<String> loggers = Arrays.stream(loggerNames).collect(Collectors.toList());
        return select(event -> loggers.contains(event.getLoggerName()));
    }

    /**
     * @param logger logger class
     * @return sub selector with filtered logs from other loggers
     */
    public LogsSelector logger(final Class<?>... logger) {
        return logger(Arrays.stream(logger).map(Class::getName).toArray(String[]::new));
    }

    /**
     * @param messagePart message to find in logged messages
     * @return sub selector with log records containing provided string
     */
    public LogsSelector containing(final String messagePart) {
        return select(event -> event.getFormattedMessage().contains(messagePart));
    }

    /**
     * @param regex regular expression
     * @return sub selector with log records matching provided regex (matched by find)
     */
    public LogsSelector matching(final String regex) {
        final Pattern pattern = Pattern.compile(regex);
        return select(event -> pattern.matcher(event.getFormattedMessage()).find());
    }
}
