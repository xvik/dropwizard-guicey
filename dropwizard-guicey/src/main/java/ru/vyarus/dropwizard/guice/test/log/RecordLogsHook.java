package ru.vyarus.dropwizard.guice.test.log;

import io.dropwizard.core.Configuration;
import io.dropwizard.core.ConfiguredBundle;
import io.dropwizard.core.setup.Environment;
import org.slf4j.event.Level;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook;
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycle;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.configuration.BeforeInitEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Record log events for verification.
 * IMPORTANT: works ONLY with logback (would be useless with another logger).
 * <p>
 * Without additional configuration would record all events (from the root logger):
 * {@code hook.record().start(Level.INFO)}
 * In most cases, it would be more convenient to listen to the exact logger logs:
 * {@code hook.record(Service.class).start(Level.INFO)} - listen Service logs (assuming logger created as
 * {@code LoggerFactory.getLogger(Service.class)}).
 * Entire packages could be listened with: {@code hook.register("com.package").start(Level.INFO)}.
 * (class and string loggers could be specified together).
 * <p>
 * Could be used for a quick logger configuration changes in tests (easy switch to TRACE, for example).
 * <p>
 * Recorded events could be inspected with {@link RecordedLogs} object:
 * {@code RecordedLogs logs = hook.record().start(Level.INFO);}.
 * Raw recorded event objects could be used ({@code logs.getEvents()}) or just string messages
 * ({@code logs.getMessages()}). There are many other methods to filter recorded logs.
 * <p>
 * Events recorded for the entire application startup. Dropwizard resets loggers two times: in application constructor
 * and just before the run phase (log configuration factory init), so logs listener appender have to be re-registered.
 * LIMITATION: would not see run phase logs of dropwizard bundles, registered BEFORE
 * {@link ru.vyarus.dropwizard.guice.GuiceBundle} (no way re-attach listener before it). For dropwizard bundles,
 * registered after guice bundle (or inside it) - all logs would be visible.
 * <p>
 * Recorded logs could be cleared either with {@link RecordedLogs#clear()} or with {@link #clearLogs()} for all
 * registered recorders.
 *
 * @author Vyacheslav Rusakov
 * @since 30.04.2025
 */
@SuppressWarnings("IllegalIdentifierName")
public class RecordLogsHook implements GuiceyConfigurationHook {

    private final List<Recorder> recorders = new ArrayList<>();
    private final AtomicInteger counter = new AtomicInteger(1);

    @Override
    @SuppressWarnings("unchecked")
    public void configure(final GuiceBundle.Builder builder) throws Exception {

        // The first re-attach called in time of hooks processing (this happens in time of GuiceBundle builder
        // finalization). This is the earliest point after application creation (logs reset in application
        // constructor - Application.bootstrapLogging)
        recorders.forEach(Recorder::attach);

        builder.listen(event -> {
            // Dropwizard resets loggers just before the run phase
            // (see logging configuration see io.dropwizard.logging.common.DefaultLoggingFactory).
            // All logs in guice bundles, registered before guice bundle would remain invisible,
            // but we can register dropwizard bundle here (directly!), which would be registered before all
            // dropwizard bundles registered through guicey, and so be able to intercept all logs for such bundles.
            // (dropwizard bundles, registered by guicey are always go before guice bundle itself because
            // dropwizard calls initialization BEFORE adding bundle - no way to register them after)
            if (event.getType().equals(GuiceyLifecycle.BeforeInit)) {
                ((BeforeInitEvent) event).getBootstrap().addBundle(new RecordedLogsTrackingBundle());
            }
        });
    }

    /**
     * Start recorder configuration. If no loggers provided then root logger would be listened (all events).
     * <p>
     * Minimal usage: {@code record().loggers(Service.class).start(Level.INFO)}.
     * Could be mixed with string-based loggers declaration:
     * {@code record().loggers(Service.class).loggers("some.string.logger").start(Level.INFO)}.
     *
     * @return builder for additional configuration
     */
    public Builder record() {
        return new Builder();
    }

    /**
     * Clear recorded logs for all registered recorders.
     */
    public void clearLogs() {
        recorders.forEach(Recorder::clear);
    }

    /**
     * Detach all registered appenders from logback loggers.
     * <p>
     * Not required as dropwizard reset all logging during application startup
     * and so stale appenders would be removed in any case before each new test.
     */
    public void destroy() {
        recorders.forEach(Recorder::destroy);
    }

    /**
     * Log recorder configuration builder.
     */
    public class Builder {
        private String name;
        private final List<Class<?>> typedLoggers = new ArrayList<>();
        private final List<String> stringLoggers = new ArrayList<>();

        /**
         * Custom name for logback appender. This might be used to better identify target appender (quite rarely
         * required).
         *
         * @param name custom appender name, used for this recorder
         * @return builder instance
         */
        public Builder recorderName(final String name) {
            this.name = name;
            return this;
        }

        /**
         * Class loggers to listen.
         *
         * @param typedLoggers loggers
         * @return builder instance
         */
        public Builder loggers(final Class<?>... typedLoggers) {
            Collections.addAll(this.typedLoggers, typedLoggers);
            return this;
        }

        /**
         * Custom logger names, not based on class name. Useful for listening for entire packages.
         *
         * @param loggers string logger names to listen for
         * @return builder instance
         */
        public Builder loggers(final String... loggers) {
            Collections.addAll(this.stringLoggers, loggers);
            return this;
        }

        /**
         * WARNING: if the current logger configuration is above the required threshold, then logger level would be
         * updated! For example, if global logger level is set to WARN, but recorder level set to DEBUG then logger
         * level would be reduced to receive all required events.
         * <p>
         * Note: returning logs access object instead of recorder itself to simplify usage: in most cases,
         * no additional attach/detach is required - only actual recorded logs access. Recorder object could
         * be easily obtained with {@link RecordedLogs#getRecorder()}.
         * <p>
         * Also, note that logs recording starts just after this method call: registration of parent hook is required
         * to properly re-bind recorders after dropwizard logs resets (dropwizard resets logging during startup).
         *
         * @param level required logging level
         * @return recorded logs access object
         */
        public RecordedLogs start(final Level level) {
            final List<String> loggers = new ArrayList<>();
            if (!typedLoggers.isEmpty()) {
                loggers.addAll(typedLoggers.stream().map(Class::getName).collect(Collectors.toList()));
            }
            loggers.addAll(stringLoggers);
            String id = name;
            if (id == null) {
                id = "Logs recorder #" + counter.getAndIncrement();
            }
            final Recorder recorder = new Recorder(id, level == null ? Level.WARN : level, loggers);
            recorders.add(recorder);

            // attach here (before application run) to gather all possible logs, but dropwizard will reset it during
            // app creation (bootstrapLogging) and during loggers configuration (DefaultLoggingFactory)
            // So it must be re-attached both just after app creation and in the run phase
            recorder.attach();

            return recorder.getRecordedLogs();
        }
    }

    /**
     * Technical bundle used to re-attach log recorders after dropwizard resets all loggers.
     */
    public final class RecordedLogsTrackingBundle implements ConfiguredBundle<Configuration> {
        @Override
        public void run(final Configuration configuration,
                        final Environment environment) throws Exception {
            recorders.forEach(Recorder::attach);
        }
    }
}
