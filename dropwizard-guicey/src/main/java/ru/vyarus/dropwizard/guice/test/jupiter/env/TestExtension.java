package ru.vyarus.dropwizard.guice.test.jupiter.env;

import org.junit.jupiter.api.extension.ExtensionContext;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.conf.ExtensionConfig;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.conf.ExtensionBuilder;

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
     *
     * @param listener listener object
     * @return builder instance for chained calls
     */
    public TestExtension listen(final TestExecutionListener listener) {
        listeners.addListener(listener);
        return this;
    }
}
