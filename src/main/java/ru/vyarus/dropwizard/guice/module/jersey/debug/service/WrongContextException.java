package ru.vyarus.dropwizard.guice.module.jersey.debug.service;

/**
 * Exception thrown when service instantiated with HK2 or guice when opposite expected.
 *
 * @author Vyacheslav Rusakov
 * @since 15.01.2016
 */
public class WrongContextException extends RuntimeException {

    public WrongContextException(final String message, final Object... args) {
        super(String.format(message, args));
    }
}
