package ru.vyarus.dropwizard.guice.test.jupiter.env.listen.lambda;

/**
 * Test listener event definition for
 * {@link ru.vyarus.dropwizard.guice.test.jupiter.env.listen.lambda.LambdaTestListener}.
 *
 * @author Vyacheslav Rusakov
 * @since 20.02.2025
 */
public enum ListenerEvent {
    /**
     * Application starting.
     */
    Starting,
    /**
     * Application started.
     */
    Started,
    /**
     * Application stopping.
     */
    Stopping,
    /**
     * Application stopped.
     */
    Stopped,
    /**
     * Before all test methods.
     */
    BeforeAll,
    /**
     * After all test methods.
     */
    AfterAll,
    /**
     * Before each test method.
     */
    BeforeEach,
    /**
     * After each test method.
     */
    AfterEach
}
