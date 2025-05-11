package ru.vyarus.dropwizard.guice.test.log;

/**
 * Recorded logs access object for {@link ru.vyarus.dropwizard.guice.test.jupiter.ext.log.RecordLogs}.
 * It might be one or multiple tracked loggers (or could be all logs if exact loggers were not configured).
 * <p>
 * To avoid tons of selection methods with different parameters, all selection methods
 * return sub-selector object for further selections. For example, to select messages by level and logger:
 * {@code logger(SomeClass.class).level(Level.DEBUG).messages()}.
 * <p>
 * Terminator methods:
 * <ul>
 * <li>{@link #count()}
 * <li>{@link #empty()}
 * <li>{@link #events()}
 * <li>{@link #messages()} (or generic {@link #messages(java.util.function.Function)})
 * <li>{@link #has(org.slf4j.event.Level)}
 * <li>{@link #has(Class)}
 * </ul>
 * <p>
 * Sub selects:
 * <ul>
 * <li>{@link #level(org.slf4j.event.Level...)}
 * <li>{@link #logger(Class[])}
 * <li>{@link #containing(String)}
 * <li>{@link #matching(String)}
 * <li>{@link #select(java.util.function.Predicate)} (generic)
 * </ul>
 *
 * @author Vyacheslav Rusakov
 * @since 26.02.2025
 */
public class RecordedLogs extends LogsSelector {

    private final Recorder recorder;

    /**
     * Create recorded logs accessor.
     *
     * @param recorder recorder
     */
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

    /**
     * @return recorder object, used to attach and detach log handlers.
     */
    public Recorder getRecorder() {
        return recorder;
    }
}
