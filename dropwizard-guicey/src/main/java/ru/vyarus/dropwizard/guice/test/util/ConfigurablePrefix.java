package ru.vyarus.dropwizard.guice.test.util;

/**
 * Interface required for custom {@link io.dropwizard.testing.ConfigOverride} implementations, used together with
 * junit 5 extensions ({@link ru.vyarus.dropwizard.guice.test.jupiter.ext.TestGuiceyAppExtension} and
 * {@link ru.vyarus.dropwizard.guice.test.jupiter.ext.TestDropwizardAppExtension}). Required because extensions
 * generate properties prefix per test to support parallel test and there is no way to know it before test
 * initialization.
 *
 * @author Vyacheslav Rusakov
 * @see ConfigOverrideValue as usage example
 * @since 06.03.2021
 */
public interface ConfigurablePrefix {

    /**
     * @param prefix current test's prefix
     */
    void setPrefix(String prefix);
}
