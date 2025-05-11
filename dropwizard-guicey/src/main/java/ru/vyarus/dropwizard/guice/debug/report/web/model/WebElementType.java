package ru.vyarus.dropwizard.guice.debug.report.web.model;

/**
 * Type of ServletModule registration.
 *
 * @author Vyacheslav Rusakov
 * @since 23.10.2019
 */
public enum WebElementType {
    /**
     * HTTP servlet.
     */
    SERVLET,
    /**
     * HTTP filter.
     */
    FILTER
}
