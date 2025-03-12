package ru.vyarus.dropwizard.guice.test.jupiter.ext.log;

import com.google.common.base.Preconditions;
import com.google.inject.Binder;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.ConfiguredBundle;
import io.dropwizard.core.setup.Environment;
import org.junit.jupiter.api.extension.ExtensionContext;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycle;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.configuration.BeforeInitEvent;
import ru.vyarus.dropwizard.guice.test.jupiter.env.field.AnnotatedField;
import ru.vyarus.dropwizard.guice.test.jupiter.env.field.AnnotatedTestFieldSetup;
import ru.vyarus.dropwizard.guice.test.jupiter.env.listen.EventContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * {@link ru.vyarus.dropwizard.guice.test.jupiter.ext.log.RecordLogs} test fields support implementation.
 * <p>
 * Applies custom appenders into required logback loggers. If required, lower loggers level (to receive all required
 * events). Note that appender applied three times: before application starts, before init (because app creation reset
 * logs) and in dropwizard run phase (because dropwizard would reset loggers during startup).
 * <p>
 * By default, collected logs cleared after each test method.
 *
 * @author Vyacheslav Rusakov
 * @since 26.02.2025
 */
public class RecordedLogsSupport extends AnnotatedTestFieldSetup<RecordLogs, RecordedLogs> {
    private static final String TEST_LOGS_FIELDS = "TEST_LOGS_FIELDS";
    private static final String FIELD_RECORDER = "FIELD_RECORDER";

    public RecordedLogsSupport() {
        super(RecordLogs.class, RecordedLogs.class, TEST_LOGS_FIELDS);
    }

    @Override
    protected void validateDeclaration(final ExtensionContext context,
                                       final AnnotatedField<RecordLogs, RecordedLogs> field) {
        // used for recorder initialization
        final RecordLogs config = field.getAnnotation();
        final List<String> loggers = new ArrayList<>(Arrays.stream(config.value())
                .map(Class::getName).collect(Collectors.toList()));
        if (config.loggers() != null) {
            Collections.addAll(loggers, config.loggers());
        }

        final Recorder recorder = new Recorder(field.toStringField(), config.level(), loggers);
        field.setCustomData(FIELD_RECORDER, recorder);
        // attach here (before application run) to gather all possible logs, but dropwizard will reset it during
        // app creation (bootstrapLogging) and during loggers configuration (DefaultLoggingFactory)
        // So it must be re-attached both just after app creation and in the run phase
        recorder.attach();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void configure(final GuiceBundle.Builder builder) {
        if (!fields.isEmpty()) {
            // The first re-attach called in time of hooks processing (this happens in time of GuiceBundle builder
            // finalization). This is the earliest point after application creation (logs reset in application
            // constructor - Application.bootstrapLogging)
            fields.forEach(field ->
                    field.<Recorder>getCustomData(FIELD_RECORDER).attach());

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
        // no super - no need bindings
    }

    @Override
    protected <K> void bindFieldValue(final Binder binder,
                                      final AnnotatedField<RecordLogs, RecordedLogs> field,
                                      final RecordedLogs value) {
        // no need
    }

    @Override
    protected <K> void bindField(final Binder binder, final AnnotatedField<RecordLogs, RecordedLogs> field) {
        // no need
    }

    @Override
    protected void validateBinding(final EventContext context, final AnnotatedField<RecordLogs, RecordedLogs> field) {
        // no need
    }

    @Override
    protected RecordedLogs getFieldValue(final EventContext context,
                                         final AnnotatedField<RecordLogs, RecordedLogs> field) {
        // use a custom object for logs selectors
        return new RecordedLogs(Preconditions.checkNotNull(field.getCustomData(FIELD_RECORDER)));
    }

    @Override
    @SuppressWarnings("PMD.SystemPrintln")
    protected void report(final EventContext context,
                          final List<AnnotatedField<RecordLogs, RecordedLogs>> annotatedFields) {
        final StringBuilder report = new StringBuilder("\nApplied log recorders (@")
                .append(RecordLogs.class.getSimpleName()).append(") on ").append(setupContextName).append("\n\n");
        fields.forEach(field -> {
            final RecordLogs config = field.getAnnotation();
            final List<String> loggers = new ArrayList<>();
            Arrays.stream(config.value()).map(Class::getSimpleName).forEach(loggers::add);
            Collections.addAll(loggers, config.loggers());
            report.append(
                    String.format("\t%-30s %-6s %s%n", '#' + field.getField().getName(), config.level(),
                            String.join(",", loggers))
            );
        });
        System.out.println(report);
    }

    @Override
    protected void beforeTest(final EventContext context,
                              final AnnotatedField<RecordLogs, RecordedLogs> field, final RecordedLogs value) {
        // no need
    }

    @Override
    protected void afterTest(final EventContext context,
                             final AnnotatedField<RecordLogs, RecordedLogs> field, final RecordedLogs value) {
        if (field.getAnnotation().autoReset()) {
            value.clear();
        }
    }

    @Override
    public void stopped(final EventContext context) {
        // last moment before field state would be cleared
        fields.forEach(field -> {
            final Recorder recorder = field.getCustomData(FIELD_RECORDER);
            if (recorder != null) {
                // detach logger (to not collect stale appenders in the root logger)
                recorder.destroy();
            }
        });
        super.stopped(context);
    }

    private final class RecordedLogsTrackingBundle implements ConfiguredBundle<Configuration> {
        @Override
        public void run(final Configuration configuration,
                        final Environment environment) throws Exception {
            fields.forEach(field ->
                    field.<Recorder>getCustomData(FIELD_RECORDER).attach());
        }
    }
}
