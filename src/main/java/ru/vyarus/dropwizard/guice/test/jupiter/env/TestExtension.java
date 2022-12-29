package ru.vyarus.dropwizard.guice.test.jupiter.env;

import ru.vyarus.dropwizard.guice.test.jupiter.ext.conf.ExtensionConfig;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.conf.ExtensionBuilder;

/**
 * Configuration object for {@link ru.vyarus.dropwizard.guice.test.jupiter.env.TestEnvironmentSetup} objects.
 *
 * @author Vyacheslav Rusakov
 * @since 15.05.2022
 */
public class TestExtension extends ExtensionBuilder<TestExtension, ExtensionConfig> {

    public TestExtension(final ExtensionConfig cfg) {
        super(cfg);
    }
}
