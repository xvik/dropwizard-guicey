package ru.vyarus.dropwizard.guice.test.jupiter.env.listen.lambda;

import ru.vyarus.dropwizard.guice.test.jupiter.env.listen.EventContext;

/**
 * Lambda version for {@link ru.vyarus.dropwizard.guice.test.jupiter.env.listen.TestExecutionListener}. Requires
 * {@link ru.vyarus.dropwizard.guice.test.jupiter.env.listen.lambda.TestExecutionListenerLambdaAdapter}. Assumed to
 * be used with {@link ru.vyarus.dropwizard.guice.test.jupiter.env.TestEnvironmentSetup} object, when its declared
 * as lambda itself and complete listenere implementation (
 * {@link ru.vyarus.dropwizard.guice.test.jupiter.env.TestExtension#listen(
 * ru.vyarus.dropwizard.guice.test.jupiter.env.listen.TestExecutionListener)}) would look clumsy.
 *
 * @author Vyacheslav Rusakov
 * @since 20.02.2025
 */
@FunctionalInterface
public interface LambdaTestListener {

    /**
     * Called on test event (see {@link ru.vyarus.dropwizard.guice.test.jupiter.env.listen.TestExecutionListener})
     * for events description.
     *
     * @param context context object providing access to all required objects
     */
    void onTestEvent(EventContext context);
}
