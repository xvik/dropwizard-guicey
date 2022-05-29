package ru.vyarus.dropwizard.guice.test.jupiter.ext.conf;

import io.dropwizard.testing.ConfigOverride;
import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook;
import ru.vyarus.dropwizard.guice.test.jupiter.env.TestEnvironmentSetup;
import ru.vyarus.dropwizard.guice.test.util.HooksUtil;
import ru.vyarus.dropwizard.guice.test.util.TestSetupUtils;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Base configuration for junit 5 extensions (contains common configurations). Required to unify common configuration
 * methods in {@link ExtensionBuilder}.
 *
 * @author Vyacheslav Rusakov
 * @since 12.05.2022
 */
@SuppressWarnings({"checkstyle:VisibilityModifier", "PMD.DefaultPackage"})
public abstract class ExtensionConfig {

    public String[] configOverrides = new String[0];
    // required for lazy evaluation values
    public final List<ConfigOverride> configOverrideObjects = new ArrayList<>();
    public final List<GuiceyConfigurationHook> hooks = new ArrayList<>();

    public final List<TestEnvironmentSetup> extensions = new ArrayList<>();
    // tracks source of registered setup objects

    public final TestExtensionsTracker tracker;

    public ExtensionConfig(final TestExtensionsTracker tracker) {
        this.tracker = tracker;
    }


    @SafeVarargs
    public final void extensionsFromAnnotation(final Class<? extends Annotation> ann,
                                               final Class<? extends TestEnvironmentSetup>... exts) {
        extensions.addAll(TestSetupUtils.create(exts));
        tracker.extensionsFromAnnotation(ann, exts);
    }

    @SafeVarargs
    public final void hooksFromAnnotation(final Class<? extends Annotation> ann,
                                          final Class<? extends GuiceyConfigurationHook>... exts) {
        hooks.addAll(HooksUtil.create(exts));
        tracker.hooksFromAnnotation(ann, exts);
    }

    public final void hookInstances(final GuiceyConfigurationHook... exts) {
        Collections.addAll(hooks, exts);
        tracker.hookInstances(exts);
    }

    @SafeVarargs
    public final void hookClasses(final Class<? extends GuiceyConfigurationHook>... exts) {
        hooks.addAll(HooksUtil.create(exts));
        tracker.hookClasses(exts);
    }
}
