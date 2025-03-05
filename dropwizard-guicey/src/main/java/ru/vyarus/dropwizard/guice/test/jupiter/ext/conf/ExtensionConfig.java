package ru.vyarus.dropwizard.guice.test.jupiter.ext.conf;

import com.google.common.base.Preconditions;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.dropwizard.core.Configuration;
import io.dropwizard.testing.ConfigOverride;
import org.junit.jupiter.api.function.ThrowingSupplier;
import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook;
import ru.vyarus.dropwizard.guice.module.installer.util.InstanceUtils;
import ru.vyarus.dropwizard.guice.test.client.TestClientFactory;
import ru.vyarus.dropwizard.guice.test.jupiter.env.TestEnvironmentSetup;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.conf.track.TestExtensionsTracker;
import ru.vyarus.dropwizard.guice.test.util.ConfigModifier;
import ru.vyarus.dropwizard.guice.test.util.ConfigOverrideUtils;
import ru.vyarus.dropwizard.guice.test.util.HooksUtil;
import ru.vyarus.dropwizard.guice.test.util.TestSetupUtils;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Base configuration for junit 5 extensions (contains common configurations). Required to unify common configuration
 * methods in {@link ExtensionBuilder}.
 *
 * @author Vyacheslav Rusakov
 * @since 12.05.2022
 */
@SuppressWarnings({"checkstyle:VisibilityModifier", "PMD.DefaultPackage", "PMD.AvoidFieldNameMatchingMethodName"})
public abstract class ExtensionConfig {

    public String[] configOverrides = new String[0];
    public ThrowingSupplier<? extends Configuration> confInstance;
    // required for lazy evaluation values
    public final List<ConfigOverride> configOverrideObjects = new ArrayList<>();
    public final List<ConfigModifier<?>> configModifiers = new ArrayList<>();
    public final List<GuiceyConfigurationHook> hooks = new ArrayList<>();
    public final List<TestEnvironmentSetup> extensions = new ArrayList<>();
    public TestClientFactory clientFactory;
    public boolean injectOnce;
    public boolean defaultExtensionsEnabled = true;
    // tracks source of registered setup objects
    public final TestExtensionsTracker tracker;

    public boolean reuseApp;
    public Class<?> reuseDeclarationClass;
    // description of declaration field or annotation (in declaration class)
    public String reuseSource;

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

    @SafeVarargs
    public final void configModifiersFromAnnotation(final Class<? extends Annotation> ann,
                                          final Class<? extends ConfigModifier<?>>... modifiers) {
        configModifiers.addAll(ConfigOverrideUtils.createModifiers(modifiers));
        tracker.configModifiersFromAnnotation(ann, modifiers);
    }

    @SafeVarargs
    public final void configModifierClasses(final Class<? extends ConfigModifier<?>>... modifiers) {
        configModifiers.addAll(ConfigOverrideUtils.createModifiers(modifiers));
        tracker.configModifierClasses(modifiers);
    }

    public final void configModifierInstances(final ConfigModifier<?>... modifiers) {
        Collections.addAll(configModifiers, modifiers);
        tracker.configModifierInstances(modifiers);
    }

    @SuppressFBWarnings("PA_PUBLIC_PRIMITIVE_ATTRIBUTE")
    public final void clientFactory(final Class<? extends TestClientFactory> factoryType) {
        try {
            this.clientFactory = InstanceUtils.create(factoryType);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to instantiate test client factory", ex);
        }
    }

    @SuppressWarnings("unchecked")
    public <C extends Configuration> C getConfiguration(final String configPath) {
        C cfg = null;
        if (confInstance != null) {
            Preconditions.checkState(configPath.isEmpty(),
                    "Configuration path can't be used with manual configuration instance: %s", configPath);
            Preconditions.checkState(configOverrides.length == 0,
                    "Configuration overrides can't be used with manual configuration instance: %s",
                    Arrays.toString(configOverrides));
            Preconditions.checkState(configOverrideObjects.isEmpty(),
                    "Configuration overrides can't be used with manual configuration instance");
            try {
                cfg = (C) confInstance.get();
            } catch (Throwable e) {
                throw new IllegalStateException("Manual configuration instance construction failed", e);
            }
            Preconditions.checkNotNull(cfg, "Configuration can't be null");
        }
        return cfg;
    }
}
