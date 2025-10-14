package ru.vyarus.dropwizard.guice.test.client.builder.call;

/**
 * Alternative to {@link ru.vyarus.dropwizard.guice.url.util.Caller} when multipart method is called.
 * Required to provide an additional helper utility to simplify multipart entities creation for method parameters.
 *
 * @param <T> resource type
 * @author Vyacheslav Rusakov
 * @since 10.10.2025
 */
@FunctionalInterface
public interface MultipartAwareCaller<T> {

    /**
     * Called to record resource method call.
     *
     * @param instance  resource mock (intercepting calls)
     * @param multipart multipart arguments helper utility
     * @throws Exception on error
     */
    void call(T instance, MultipartArgumentHelper multipart) throws Exception;
}
