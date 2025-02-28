package ru.vyarus.dropwizard.guice.test.jupiter.ext.log;

/**
 * Recorded logs access object for {@link ru.vyarus.dropwizard.guice.test.jupiter.ext.log.RecordLogs}.
 * It might be one or multiple tracked loggers (or could be all logs if exact loggers were not configured).
 * <p>
 * To avoid tons of selection methods with different parameters, all selection methods
 * return sub-selector object for further selections. For example, to select messages by level and logger:
 * {@code logger(SomeClass.class).level(Level.DEBUG).messages()}.
 * <p>
 * Terminator methods:
 *  - {@link #count()}
 *  - {@link #empty()}
 *  - {@link #events()}
 *  - {@link #messages()} (or generic {@link #messages(java.util.function.Function)})
 *  - {@link #has(org.slf4j.event.Level)}
 *  - {@link #has(Class)}
 * <p>
 * Sub selects:
 *  - {@link #level(org.slf4j.event.Level...)}
 *  - {@link #logger(Class[])}
 *  - {@link #containing(String)}
 *  - {@link #matching(String)}
 *  - {@link #select(java.util.function.Predicate)} (generic)
 *
 * @author Vyacheslav Rusakov
 * @since 26.02.2025
 */
public class RecordedLogs extends LogsSelector {

    private final Recorder recorder;

    public RecordedLogs(final Recorder recorder) {
        super(recorder.getRecords());
        this.recorder = recorder;
    }

    /**
     * Clear collected recordings.
     */
    public void clear() {
        recorder.clear();
    }
}
