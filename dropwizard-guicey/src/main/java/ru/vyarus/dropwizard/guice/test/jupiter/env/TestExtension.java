package ru.vyarus.dropwizard.guice.test.jupiter.env;

import io.dropwizard.core.Configuration;
import org.junit.jupiter.api.extension.ExtensionContext;
import ru.vyarus.dropwizard.guice.test.jupiter.env.listen.TestExecutionListener;
import ru.vyarus.dropwizard.guice.test.jupiter.env.listen.lambda.ListenerEvent;
import ru.vyarus.dropwizard.guice.test.jupiter.env.listen.lambda.LambdaTestListener;
import ru.vyarus.dropwizard.guice.test.jupiter.env.listen.lambda.TestExecutionListenerLambdaAdapter;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.conf.ExtensionBuilder;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.conf.ExtensionConfig;
import ru.vyarus.dropwizard.guice.test.jupiter.env.field.AnnotatedField;
import ru.vyarus.dropwizard.guice.test.jupiter.env.field.TestFieldUtils;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * Configuration object for {@link ru.vyarus.dropwizard.guice.test.jupiter.env.TestEnvironmentSetup} objects.
 *
 * @author Vyacheslav Rusakov
 * @since 15.05.2022
 */
// no configuration parameter because it could brake existing code (without declared generic types work incorrectly)
public class TestExtension extends ExtensionBuilder<Configuration, TestExtension, ExtensionConfig> {

    private final ExtensionContext context;
    private final ListenersSupport listeners;
    private TestExecutionListenerLambdaAdapter listenerAdapter;

    /**
     * Create extension.
     *
     * @param cfg       config
     * @param context   junit context
     * @param listeners listeners support
     */
    public TestExtension(final ExtensionConfig cfg,
                         final ExtensionContext context,
                         final ListenersSupport listeners) {
        super(cfg);
        this.context = context;
        this.listeners = listeners;
    }

    /**
     * Useful to bind debug options on the extension debug (no need for additional keys).
     *
     * @return true if debug is enabled on guicey extension
     */
    public boolean isDebug() {
        return cfg.tracker.debug;
    }

    /**
     * Shortcut to simplify detection on what phase extension was created: beforeAll or beforeEach.
     *
     * @return true if application started once per test class, false if application started for each test method
     */
    public boolean isApplicationStartedForClass() {
        return getJunitContext().getTestMethod().isEmpty();
    }

    /**
     * This might be class or method context ("before all" or "before each"), depending on how guicey extension would
     * be registered: in case of registration with a non-static field "before all" not called.
     * <p>
     * Note that guicey provide static method for accessing objects, stored in context, like:
     * {@link ru.vyarus.dropwizard.guice.test.jupiter.ext.GuiceyExtensionsSupport#lookupSupport(
     * org.junit.jupiter.api.extension.ExtensionContext)} or
     * {@link ru.vyarus.dropwizard.guice.test.jupiter.ext.GuiceyExtensionsSupport#lookupInjector(
     * org.junit.jupiter.api.extension.ExtensionContext)}.
     *
     * @return test extension context
     */
    public ExtensionContext getJunitContext() {
        return context;
    }

    /**
     * Listen for test lifecycle. Useful when not only resource close is required (achievable by returning
     * a closable object from setup), but writing a separate junit extension is not desirable.
     * Moreover, this listener is synchronized with guicey extension lifecycle.
     * <p>
     * Listener could also be registered with lambdas using on* methods like
     * {@link #onApplicationStart(ru.vyarus.dropwizard.guice.test.jupiter.env.listen.lambda.LambdaTestListener)}.
     * Lambda version might be more convenient in case when setup object is a lambda itself.
     *
     * @param listener listener object
     * @return builder instance for chained calls
     */
    public TestExtension listen(final TestExecutionListener listener) {
        listeners.addListener(listener);
        return this;
    }

    /**
     * Lambda version of {@link #listen(ru.vyarus.dropwizard.guice.test.jupiter.env.listen.TestExecutionListener)}
     * for {@link ru.vyarus.dropwizard.guice.test.jupiter.env.listen.TestExecutionListener#starting(
     * ru.vyarus.dropwizard.guice.test.jupiter.env.listen.EventContext)}. Lambda listener version is more useful in
     * case when setup object is declared as a lambda itself.
     * <p>
     * Might be called multiple times.
     *
     * @param listener listener called before application start (could be beforeAll (default) or beforeEach phase)
     * @return builder instance for chained calls
     */
    public TestExtension onApplicationStarting(final LambdaTestListener listener) {
        registerListener(ListenerEvent.Starting, listener);
        return this;
    }

    /**
     * Lambda version of {@link #listen(ru.vyarus.dropwizard.guice.test.jupiter.env.listen.TestExecutionListener)}
     * for {@link ru.vyarus.dropwizard.guice.test.jupiter.env.listen.TestExecutionListener#started(
     * ru.vyarus.dropwizard.guice.test.jupiter.env.listen.EventContext)}. Lambda listener version is more useful in
     * case when setup object is declared as a lambda itself.
     * <p>
     * Might be called multiple times.
     *
     * @param listener listener called after application start (could be beforeAll (default) or beforeEach phase)
     * @return builder instance for chained calls
     */
    public TestExtension onApplicationStart(final LambdaTestListener listener) {
        registerListener(ListenerEvent.Started, listener);
        return this;
    }

    /**
     * Lambda version of {@link #listen(ru.vyarus.dropwizard.guice.test.jupiter.env.listen.TestExecutionListener)}
     * for {@link ru.vyarus.dropwizard.guice.test.jupiter.env.listen.TestExecutionListener#beforeAll(
     * ru.vyarus.dropwizard.guice.test.jupiter.env.listen.EventContext)}. Lambda listener version is more useful in
     * case when setup object is declared as a lambda itself.
     * <p>
     * Might be called multiple times.
     *
     * @param listener listener called (might not be called!) before all test methods
     * @return builder instance for chained calls
     */
    public TestExtension onBeforeAll(final LambdaTestListener listener) {
        registerListener(ListenerEvent.BeforeAll, listener);
        return this;
    }

    /**
     * Lambda version of {@link #listen(ru.vyarus.dropwizard.guice.test.jupiter.env.listen.TestExecutionListener)}
     * for {@link ru.vyarus.dropwizard.guice.test.jupiter.env.listen.TestExecutionListener#beforeEach(
     * ru.vyarus.dropwizard.guice.test.jupiter.env.listen.EventContext)}. Lambda listener version is more useful in
     * case when setup object is declared as a lambda itself.
     * <p>
     * Might be called multiple times.
     *
     * @param listener listener called before each test method
     * @return builder instance for chained calls
     */
    public TestExtension onBeforeEach(final LambdaTestListener listener) {
        registerListener(ListenerEvent.BeforeEach, listener);
        return this;
    }

    /**
     * Lambda version of {@link #listen(ru.vyarus.dropwizard.guice.test.jupiter.env.listen.TestExecutionListener)}
     * for {@link ru.vyarus.dropwizard.guice.test.jupiter.env.listen.TestExecutionListener#afterEach(
     * ru.vyarus.dropwizard.guice.test.jupiter.env.listen.EventContext)}. Lambda listener version is more useful in
     * case when setup object is declared as a lambda itself.
     * <p>
     * Might be called multiple times.
     *
     * @param listener listener called after each test method
     * @return builder instance for chained calls
     */
    public TestExtension onAfterEach(final LambdaTestListener listener) {
        registerListener(ListenerEvent.AfterEach, listener);
        return this;
    }

    /**
     * Lambda version of {@link #listen(ru.vyarus.dropwizard.guice.test.jupiter.env.listen.TestExecutionListener)}
     * for {@link ru.vyarus.dropwizard.guice.test.jupiter.env.listen.TestExecutionListener#afterAll(
     * ru.vyarus.dropwizard.guice.test.jupiter.env.listen.EventContext)}. Lambda listener version is more useful in
     * case when setup object is declared as a lambda itself.
     * <p>
     * Might be called multiple times.
     *
     * @param listener listener called (might not be called!) after all test methods
     * @return builder instance for chained calls
     */
    public TestExtension onAfterAll(final LambdaTestListener listener) {
        registerListener(ListenerEvent.AfterAll, listener);
        return this;
    }

    /**
     * Lambda version of {@link #listen(ru.vyarus.dropwizard.guice.test.jupiter.env.listen.TestExecutionListener)}
     * for {@link ru.vyarus.dropwizard.guice.test.jupiter.env.listen.TestExecutionListener#stopping(
     * ru.vyarus.dropwizard.guice.test.jupiter.env.listen.EventContext)}. Lambda listener version is more useful in
     * case when setup object is declared as a lambda itself.
     * <p>
     * Might be called multiple times.
     *
     * @param listener listener called before application stop (could be afterAll (default) or afterEach phase)
     * @return builder instance for chained calls
     */
    public TestExtension onApplicationStopping(final LambdaTestListener listener) {
        registerListener(ListenerEvent.Stopping, listener);
        return this;
    }

    /**
     * Lambda version of {@link #listen(ru.vyarus.dropwizard.guice.test.jupiter.env.listen.TestExecutionListener)}
     * for {@link ru.vyarus.dropwizard.guice.test.jupiter.env.listen.TestExecutionListener#stopped(
     * ru.vyarus.dropwizard.guice.test.jupiter.env.listen.EventContext)}. Lambda listener version is more useful in
     * case when setup object is declared as a lambda itself.
     * <p>
     * Might be called multiple times.
     *
     * @param listener listener called after application stop (could be afterAll (default) or afterEach phase)
     * @return builder instance for chained calls
     */
    public TestExtension onApplicationStop(final LambdaTestListener listener) {
        registerListener(ListenerEvent.Stopped, listener);
        return this;
    }

    /**
     * Search for annotated fields with validation (a field type must be assignable to provided type).
     *
     * @param annotation        annotation to search
     * @param requiredFieldType required field type
     * @param <A>               annotation type
     * @param <T>               required filed minimal type
     * @return annotated test fields (including fields from base test class).
     */
    public <A extends Annotation, T> List<AnnotatedField<A, T>> findAnnotatedFields(
            final Class<A> annotation,
            final Class<T> requiredFieldType) {
        return TestFieldUtils.findAnnotatedFields(context.getRequiredTestClass(), annotation, requiredFieldType);
    }

    private void registerListener(final ListenerEvent event, final LambdaTestListener listener) {
        // create adapter on demand and register once - all other lambdas will refer to the same adapter instance
        if (listenerAdapter == null) {
            listenerAdapter = new TestExecutionListenerLambdaAdapter();
            listen(listenerAdapter);
        }
        listenerAdapter.listen(event, listener);
    }
}
