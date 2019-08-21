package ru.vyarus.dropwizard.guice.module.context.debug.report.guice.util.visitor;

import com.google.inject.spi.PrivateElements;

/**
 * Marker exception indicating private module binding.
 *
 * @author Vyacheslav Rusakov
 * @since 21.08.2019
 */
public class PrivateModuleException extends RuntimeException {

    private final PrivateElements elements;

    public PrivateModuleException(final PrivateElements elements) {
        this.elements = elements;
    }

    public PrivateElements getElements() {
        return elements;
    }
}
