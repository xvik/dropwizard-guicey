package ru.vyarus.dropwizard.guice.test.jupiter.ext.log;

import org.slf4j.event.Level;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Record log events for verification.
 * IMPORTANT: works ONLY with logback (would be useless with another logger).
 * <p>
 * Without additional configuration would record all events (from the root logger):
 * {@code @RecordLogs RecordedLogs logs}.
 * In most cases, it would be more convenient to listen to the exact logger logs:
 * {@code @RecordLogs(Service.class) RecordedLogs logs} - listen Service logs (assuming logger created as
 * {@code LoggerFactory.getLogger(Service.class)}).
 * Entire packages could be listened with: {@code @RecordLogs(listeners = "com.package") RecordedLogs logs}.
 * (class and string loggers could be specified together).
 * <p>
 * By default, listen WARN logs and above. To set a different level use
 * {@code @RecordLogs(value = Service.class level = Level.INFO) RecordedLogs logs}.
 * NOTE that logger level would be decreased (re-configured) to receive events from the required threshold.
 * <p>
 * Could be used for a quick logger configuration changes in tests (easy switch to TRACE, for example).
 * <p>
 * Recorded events could be inspected with {@link RecordedLogs} object: {@code logs.getEvents()} for raw event
 * objects or {@code logs.getMessages()} for logged messages. There are many other methods to filter events.
 * <p>
 * Events recorded for the entire application startup. Dropwizard resets loggers two times: in application constructor
 * and just before the run phase (log configuration factory init), so logs listener appender have to be re-registered.
 * LIMITATION: would not see run phase logs of dropwizard bundles, registered BEFORE
 * {@link ru.vyarus.dropwizard.guice.GuiceBundle} (no way re-attach listener before it). For dropwizard bundles,
 * registered after guice bundle (or inside it) - all logs would be visible
 * <p>
 * Recorded logs are cleared after each test. Use {@link #autoReset()} to disable. Also, clean could be performed
 * manually with {@link RecordedLogs#clear()}.
 *
 * @author Vyacheslav Rusakov
 * @since 26.02.2025
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface RecordLogs {

    /**
     * Classes to track loggers for. All log events would be recorded when empty.
     * <p>
     * For string logger names use {@link #loggers()} (could be used together with class loggers).
     *
     * @return logger classes to listen for
     */
    Class<?>[] value() default {};

    /**
     * Custom logger names, not based on class name. Useful for listening for entire packages.
     * <p>
     * Works with class loggers ({@link #value()}).
     *
     * @return string logger names to listen for
     */
    String[] loggers() default {};

    /**
     * WARNING: if the current logger configuration is above the required threshold, then logger level would be updated!
     * For example, if global logger level is set to WARN, but recorder level set to DEBUG then logger level would
     * be reduced to receive all required events.
     *
     * @return required events threshold
     */
    Level level() default Level.WARN;

    /**
     * By default, recorded event reset after each test method. Use to disable automatic cleanup. Note that
     * events could be cleared directly with {@link RecordedLogs#clear()}.
     *
     * @return true to clean up recorded events after test
     */
    boolean autoReset() default true;
}
