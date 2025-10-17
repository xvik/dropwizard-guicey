package ru.vyarus.dropwizard.guice.test.jupiter.ext.client.rest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Inject client for exact resource class. The same as {@code ClientSupport.restClient(Resource.class)}.
 * <p>
 * Usage: {@code @WebResourceClient ResourceClient<Resource> rest}.
 * <p>
 * The same could be achieved with the root rest client (or client support object):
 * <pre>{@code @WebClient(REST) TestClient rest;
 *  ResourceClient res = rest.restClient(Resource.class)
 * }</pre>
 * <p>
 * Works with both integration tests and stub rest.
 *
 * @author Vyacheslav Rusakov
 * @since 17.10.2025
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface WebResourceClient {

    /**
     * Reset client defaults after each test method. Enabled by default.
     *
     * @return false to disable defaults reset
     */
    boolean autoReset() default true;
}
