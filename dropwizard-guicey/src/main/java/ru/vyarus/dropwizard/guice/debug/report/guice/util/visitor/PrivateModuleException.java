package ru.vyarus.dropwizard.guice.debug.report.guice.util.visitor;

import com.google.inject.spi.PrivateElements;

/**
 * Marker exception indicating private module binding.
 *
 * @author Vyacheslav Rusakov
 * @since 21.08.2019
 */
public class PrivateModuleException extends RuntimeException {

    /**
     * Private elements.
     */
    private final PrivateElements elements;

    /**
     * Create exception.
     *
     * @param elements private elements
     */
    public PrivateModuleException(final PrivateElements elements) {
        this.elements = elements;
    }

    /**
     * @return private elements
     */
    public PrivateElements getElements() {
        return elements;
    }
}
