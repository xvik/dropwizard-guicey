package ru.vyarus.dropwizard.guice.url.util;

/**
 * Interface used for recording of resource method call.
 *
 * @author Vyacheslav Rusakov
 * @since 30.09.2025
 * @param <T> instance type
 */
@FunctionalInterface
public interface Caller<T> {

    /**
     * Record instance method call.
     *
     * @param instance object proxy (to record call on)
     * @throws Exception bypass all exceptions
     */
    void call(T instance) throws Exception;
}
