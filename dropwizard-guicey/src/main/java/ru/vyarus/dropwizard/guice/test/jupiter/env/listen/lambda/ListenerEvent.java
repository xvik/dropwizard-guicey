package ru.vyarus.dropwizard.guice.test.jupiter.env.listen.lambda;

/**
 * Test listener event definition for
 * {@link ru.vyarus.dropwizard.guice.test.jupiter.env.listen.lambda.LambdaTestListener}.
 *
 * @author Vyacheslav Rusakov
 * @since 20.02.2025
 */
public enum ListenerEvent {
    Started, Stopped, BeforeAll, AfterAll, BeforeEach, AfterEach
}
