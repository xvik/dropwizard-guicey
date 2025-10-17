package ru.vyarus.dropwizard.guice.test.jupiter.ext.client;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Inject web client (derived from {@link ru.vyarus.dropwizard.guice.test.ClientSupport}) into test field.
 * <ul>
 *     <li>{@code @WebClient ClientSupport client}</li>
 *     <li>{@code @WebClient(APP) TestClient app} - same as {@code client.appClient()}</li>
 *     <li>{@code @WebClient(ADMIN) TestClient admin} - same as {@code client.adminClient()}</li>
 *     <li>{@code @WebClient(REST) TestClient rest} - same as {@code client.restClient()}</li>
 * </ul>
 *
 * @author Vyacheslav Rusakov
 * @since 15.10.2025
 * @see ru.vyarus.dropwizard.guice.test.jupiter.ext.client.rest.WebResourceClient for direct resource client injection
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface WebClient {

    /**
     * @return client type (default for root object and other for specific clients)
     */
    WebClientType value() default WebClientType.Support;

    /**
     * Reset client defaults after each test method. Enabled by default.
     *
     * @return false to disable defaults reset
     */
    boolean autoReset() default true;
}
