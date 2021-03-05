package ru.vyarus.dropwizard.guice.test.util;

import com.google.common.base.Preconditions;
import io.dropwizard.testing.ConfigOverride;

import java.util.function.Supplier;

/**
 * Class is a copy of {@link io.dropwizard.testing.ConfigOverrideValue}, but with configurable prefix.
 * This is required for junit 5 extensions because prefix is auto-generated for each test. Must be used with
 * {@link ru.vyarus.dropwizard.guice.test.jupiter.ext.TestGuiceyAppExtension} or
 * {@link ru.vyarus.dropwizard.guice.test.jupiter.ext.TestDropwizardAppExtension}.
 *
 * @author Vyacheslav Rusakov
 * @since 06.03.2021
 */
public class ConfigOverrideValue extends ConfigOverride implements ConfigurablePrefix {

    private static final String DOT = ".";

    private final String key;
    private final Supplier<String> value;
    private String prefix;
    private String originalValue;

    public ConfigOverrideValue(final String key, final Supplier<String> value) {
        this.key = Preconditions.checkNotNull(key, "Property name required");
        this.value = Preconditions.checkNotNull(value, "Value supplier required");
    }

    @Override
    public void setPrefix(final String prefix) {
        this.prefix = prefix.endsWith(DOT) ? prefix : prefix + DOT;
    }

    @Override
    public void addToSystemProperties() {
        Preconditions.checkNotNull(prefix, "Prefix is not defined");
        this.originalValue = System.setProperty(prefix + key, value.get());
    }

    @Override
    public void removeFromSystemProperties() {
        if (originalValue != null) {
            System.setProperty(prefix + key, originalValue);
        } else {
            System.clearProperty(prefix + key);
        }
    }
}
