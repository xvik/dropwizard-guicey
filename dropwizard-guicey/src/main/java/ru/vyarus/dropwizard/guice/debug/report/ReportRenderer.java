package ru.vyarus.dropwizard.guice.debug.report;

/**
 * Report renderer.
 *
 * @param <T> config object type
 * @author Vyacheslav Rusakov
 * @since 01.08.2016
 */
public interface ReportRenderer<T> {

    /**
     * Renders report according ro provided config.
     *
     * @param config config object
     * @return rendered report string or null (or empty) if nothing rendered
     */
    String renderReport(T config);
}
