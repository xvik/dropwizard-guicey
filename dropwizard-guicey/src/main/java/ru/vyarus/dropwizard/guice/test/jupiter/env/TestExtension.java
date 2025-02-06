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

    public TestExtension(final ExtensionConfig cfg, final ExtensionContext context) {
        super(cfg);
        this.context = context;
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
}
