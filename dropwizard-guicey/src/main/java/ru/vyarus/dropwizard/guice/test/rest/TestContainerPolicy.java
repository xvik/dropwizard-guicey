package ru.vyarus.dropwizard.guice.test.rest;

/**
 * Jersey test container implementation selection policy.
 *
 * @author Vyacheslav Rusakov
 * @since 25.02.2025
 */
public enum TestContainerPolicy {

    /**
     * Use grizzly, if available in classpath, otherwise use in memory container. Also, factory could be specified
     * in {@link org.glassfish.jersey.test.TestProperties#CONTAINER_FACTORY} system property (see
     * {@link org.glassfish.jersey.test.JerseyTest#getDefaultTestContainerFactory()})
     */
    DEFAULT,
    /**
     * Use {@link org.glassfish.jersey.test.inmemory.InMemoryTestContainerFactory}, even if grizzly is available in
     * classpath. In-memory is a lightweight rest container, but it does not support all features. If not supported
     * features requires - use grizzly.
     */
    IN_MEMORY,
    /**
     * Use {@code org.glassfish.jersey.test.grizzly.GrizzlyTestContainerFactory}. Fail with a descriptive message
     * if factory is not found in classpath. Useful to prevent accident in-memory container usage.
     */
    GRIZZLY
}
