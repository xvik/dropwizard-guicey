package ru.vyarus.guicey.gsp.views.template;

/**
 * Exception thrown when template not found in configured classpath locations.
 *
 * @author Vyacheslav Rusakov
 * @since 07.12.2018
 */
public class TemplateNotFoundException extends RuntimeException {

    /**
     * Create a not found exception.
     *
     * @param message message
     */
    public TemplateNotFoundException(final String message) {
        super(message);
    }
}
