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
    protected static final String NEWLINE = String.format("%n");
    protected static final String TAB = "    ";

    // marker to be able switch off reports easily
    private static final Marker MARKER = MarkerFactory.getMarker("installer reporter");

    private Logger logger;
    private StringBuilder message = new StringBuilder();
    private int counter;

    public Reporter(final Class<? extends FeatureInstaller> type, final String title) {
        this.logger = LoggerFactory.getLogger(type);
        message.append(title).append(NEWLINE).append(NEWLINE);
    }

    /**
     * Prints formatted line.
     *
     * @param line line with {@code String.format} syntax
     * @param args message arguments
     * @return reporter instance
     */
    public Reporter line(final String line, final Object... args) {
        counter++;
        message.append(TAB).append(String.format(line, args)).append(NEWLINE);
        return this;
    }

    /**
     * Prints empty line.
     *
     * @return reporter instance
     */
    public Reporter emptyLine() {
        message.append(NEWLINE);
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
        // free memory
        message = null;
        logger = null;
    }
}
