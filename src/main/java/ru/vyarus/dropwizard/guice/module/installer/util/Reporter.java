package ru.vyarus.dropwizard.guice.module.installer.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import ru.vyarus.dropwizard.guice.module.installer.FeatureInstaller;

/**
 * Helper class to simplify installers dropwizard style report building.
 *
 * @author Vyacheslav Rusakov
 * @since 12.10.2014
 */
@SuppressWarnings("PMD.AvoidStringBufferField")
public class Reporter {
    public static final String NEWLINE = String.format("%n");
    public static final String TAB = "    ";
    protected static final String LAZY_MARKER = " *LAZY_MARKER";

    // marker to be able switch off reports easily
    private static final Marker MARKER = MarkerFactory.getMarker("installer reporter");

    private final Logger logger;
    private StringBuilder message;
    private int counter;
    private boolean wasEmptyLine;

    public Reporter(final Class<? extends FeatureInstaller> type, final String title) {
        this.logger = LoggerFactory.getLogger(type);
        this.message = new StringBuilder();
        message.append(title).append(NEWLINE);
        emptyLine();
    }

    /**
     * Supports {@code @LazyBinding} annotation, to show lazy marks in report.
     *
     * @param isLazy lazy marker
     * @return empty string if lazy not set, lazy marker if lazy set.
     */
    public final String lazy(final boolean isLazy) {
        return isLazy ? LAZY_MARKER : "";
    }

    /**
     * Prints formatted line.
     *
     * @param line line with {@code String.format} syntax
     * @param args message arguments
     * @return reporter instance
     */
    public final Reporter line(final String line, final Object... args) {
        counter++;
        wasEmptyLine = false;
        message.append(TAB).append(String.format(line, args)).append(NEWLINE);
        return this;
    }

    /**
     * Prints empty line.
     *
     * @return reporter instance
     */
    public final Reporter emptyLine() {
        wasEmptyLine = true;
        message.append(NEWLINE);
        return this;
    }

    /**
     * Writes empty line if something was printed before and it was not {@link #emptyLine()}.
     *
     * @return reporter instance
     */
    public final Reporter separate() {
        if (message.length() > 0 && !wasEmptyLine) {
            emptyLine();
        }
        return this;
    }

    /**
     * Prints composed report into logger or do nothing if no lines were provided.
     */
    @SuppressWarnings("PMD.NullAssignment")
    public void report() {
        if (counter > 0) {
            logger.info(MARKER, message.toString());
        }
        message = new StringBuilder();
    }
}
