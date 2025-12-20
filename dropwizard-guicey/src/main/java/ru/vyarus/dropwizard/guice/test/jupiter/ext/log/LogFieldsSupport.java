package ru.vyarus.dropwizard.guice.test.jupiter.ext.log;

import com.google.common.base.Preconditions;
import org.junit.jupiter.api.extension.ExtensionContext;
import ru.vyarus.dropwizard.guice.test.jupiter.env.TestExtension;
import ru.vyarus.dropwizard.guice.test.jupiter.env.field.AnnotatedField;
import ru.vyarus.dropwizard.guice.test.jupiter.env.field.AnnotatedTestFieldSetup;
import ru.vyarus.dropwizard.guice.test.jupiter.env.listen.EventContext;
import ru.vyarus.dropwizard.guice.test.log.RecordLogsHook;
import ru.vyarus.dropwizard.guice.test.log.RecordedLogs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
public class LogFieldsSupport extends AnnotatedTestFieldSetup<RecordLogs, RecordedLogs> {
    private static final String TEST_LOGS_FIELDS = "TEST_LOGS_FIELDS";
    private static final String FIELD_RECORDER = "FIELD_RECORDER";

    private final RecordLogsHook hook = new RecordLogsHook();

    /**
     * Create support.
     */
    public LogFieldsSupport() {
        super(RecordLogs.class, RecordedLogs.class, TEST_LOGS_FIELDS);
    }

    @Override
    protected void fieldDetected(final ExtensionContext context,
                                 final AnnotatedField<RecordLogs, RecordedLogs> field) {
        // recorder initialization
        final RecordLogs config = field.getAnnotation();
        final RecordedLogs logs = hook.record()
                .loggers(config.value())
                .loggers(config.loggers())
                // clearly identify appender using field name (for debugging purposes)
                .recorderName(field.toStringField())
                .start(config.level());
        field.setCustomData(FIELD_RECORDER, logs);
    }

    @Override

    protected void registerHooks(final TestExtension extension) {
        extension.hooks(hook);
    }

    @Override
    protected <K> void initializeField(final AnnotatedField<RecordLogs, RecordedLogs> field,
                                       final RecordedLogs userValue) {
        // no need
    }

    @Override
    protected void beforeValueInjection(final EventContext context,
                                        final AnnotatedField<RecordLogs, RecordedLogs> field) {
        // no need
    }

    @Override
    protected RecordedLogs injectFieldValue(final EventContext context,
                                            final AnnotatedField<RecordLogs, RecordedLogs> field) {
        // use a custom object for logs selectors
        return Preconditions.checkNotNull(field.getCustomData(FIELD_RECORDER));
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
        if (field.getAnnotation().autoReset() && value != null) {
            value.clear();
        }
    }

    @Override
    public void stopped(final EventContext context) {
        // last moment before field state would be cleared
        fields.forEach(field -> {
            final RecordedLogs logs = field.getCustomData(FIELD_RECORDER);
            if (logs != null) {
                // detach logger (to not collect stale appenders in the root logger)
                logs.getRecorder().destroy();
            }
        });
        super.stopped(context);
    }
}
