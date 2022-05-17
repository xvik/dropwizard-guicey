package ru.vyarus.dropwizard.guice.test.jupiter.ext.conf;

import io.dropwizard.testing.ConfigOverride;
import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook;
import ru.vyarus.dropwizard.guice.test.jupiter.env.TestEnvironmentSetup;

import java.util.ArrayList;
import java.util.List;

/**
 * Base configuration for junit 5 extensions (contains common configurations). Required to unify common configuration
 * methods in {@link ru.vyarus.dropwizard.guice.test.jupiter.ext.conf.ExtensionBuilder}.
 *
 * @author Vyacheslav Rusakov
 * @since 12.05.2022
 */
@SuppressWarnings({"checkstyle:VisibilityModifier", "PMD.DefaultPackage", "PMD.AbstractClassWithoutAnyMethod"})
public abstract class ExtensionConfig {

    public String[] configOverrides = new String[0];
    // required for lazy evaluation values
    public List<ConfigOverride> configOverrideObjects = new ArrayList<>();
    public List<GuiceyConfigurationHook> hooks;

    public List<TestEnvironmentSetup> extensions;
}
