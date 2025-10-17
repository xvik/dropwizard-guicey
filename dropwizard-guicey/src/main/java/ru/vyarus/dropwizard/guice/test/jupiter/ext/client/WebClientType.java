package ru.vyarus.dropwizard.guice.test.jupiter.ext.client;

/**
 * Client type for {@link ru.vyarus.dropwizard.guice.test.jupiter.ext.client.WebClient} annotation.
 *
 * @author Vyacheslav Rusakov
 * @since 15.10.2025
 */
public enum WebClientType {
    /**
     * Root {@link ru.vyarus.dropwizard.guice.test.ClientSupport} client (used to obtain other clients).
     * This is also a client, targeted application root (may be different from actual application mapping).
     */
    Support,
    /**
     * Application client ({@link ru.vyarus.dropwizard.guice.test.ClientSupport#appClient()}).
     */
    App,
    /**
     * Admin client ({@link ru.vyarus.dropwizard.guice.test.ClientSupport#adminClient()}).
     */
    Admin,
    /**
     * Rest client ({@link ru.vyarus.dropwizard.guice.test.ClientSupport#restClient()}).
     */
    Rest
}
