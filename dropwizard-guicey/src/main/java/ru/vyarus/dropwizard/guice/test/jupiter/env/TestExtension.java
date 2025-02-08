package ru.vyarus.dropwizard.guice.test.jupiter.env;

import org.junit.jupiter.api.extension.ExtensionContext;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.conf.ExtensionBuilder;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.conf.ExtensionConfig;
import ru.vyarus.dropwizard.guice.test.util.FieldAccess;
import ru.vyarus.dropwizard.guice.test.util.TestFieldUtils;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * Configuration object for {@link ru.vyarus.dropwizard.guice.test.jupiter.env.TestEnvironmentSetup} objects.
 *
 * @author Vyacheslav Rusakov
 * @since 15.05.2022
 */
public class TestExtension extends ExtensionBuilder<TestExtension, ExtensionConfig> {

    private final ExtensionContext context;
    private final ListenersSupport listeners;

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
        return getJunitContext().getTestMethod().isPresent();
    }

    /**
     * This might be class or method context ("before all" or "before each"), depending on how guicey extension would
     * be registered: in case of registration with a non-static field "before all" not called.
     * <p>
     * Note that guicey provide static method for accessing objects, stored in context, like:
     * {@link ru.vyarus.dropwizard.guice.test.jupiter.ext.GuiceyExtensionsSupport#lookupSupport(
     *org.junit.jupiter.api.extension.ExtensionContext)} or
     * {@link ru.vyarus.dropwizard.guice.test.jupiter.ext.GuiceyExtensionsSupport#lookupInjector(
     *org.junit.jupiter.api.extension.ExtensionContext)}.
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
     * If {@link ru.vyarus.dropwizard.guice.test.GuiceyTestSupport} object implements listener interface directly,
     * it would be registered as listener automatically. Still, manual registration ({@code listen(this)}) would
     * not be a mistake (no duplicate appears).
     *
     * @param listener listener object
     * @return builder instance for chained calls
     */
    public TestExtension listen(final TestExecutionListener listener) {
        listeners.addListener(listener);
        return this;
    }

    /**
     * @param annotation annotation to search
     * @param <A>        annotation type
     * @return annotated test fields (including fields from base test class).
     */
    public <A extends Annotation> List<FieldAccess<A, Object>> findAnnotatedFields(
            final Class<A> annotation) {
        return findAnnotatedFields(annotation, Object.class);
    }

    /**
     * Search for annotated fields with validation (a field type must be assignable to provided type)
     *
     * @param annotation        annotation to search
     * @param requiredFieldType required field type
     * @param <A>               annotation type
     * @param <T>               required filed minimal type
     * @return annotated test fields (including fields from base test class).
     */
    public <A extends Annotation, T> List<FieldAccess<A, T>> findAnnotatedFields(
            final Class<A> annotation,
            final Class<T> requiredFieldType) {
        return TestFieldUtils.findAnnotatedFields(context.getRequiredTestClass(), annotation, requiredFieldType);
    }
}
