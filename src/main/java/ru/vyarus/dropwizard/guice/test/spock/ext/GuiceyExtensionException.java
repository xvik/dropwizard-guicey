package ru.vyarus.dropwizard.guice.test.spock.ext;

import org.spockframework.runtime.extension.ExtensionException;

/**
 * Exception thrown in case of exceptional situations in extensions.
 *
 * @author Vyacheslav Rusakov
 * @since 02.01.2015
 */
public class GuiceyExtensionException extends ExtensionException {
    public GuiceyExtensionException(final String message) {
        super(message);
    }

    public GuiceyExtensionException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
