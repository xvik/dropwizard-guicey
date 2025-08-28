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
@SuppressWarnings({"checkstyle:VisibilityModifier", "PMD.AvoidFieldNameMatchingMethodName"})
public abstract class ExtensionConfig {

    /**
     * Configuration overrides.
     */
    public String[] configOverrides = new String[0];
    /**
     * Configuration instance supplier.
     */
    public ThrowingSupplier<? extends Configuration> confInstance;
    /**
     * Configuration override object. Required for lazy evaluation values
     */
    public final List<ConfigOverride> configOverrideObjects = new ArrayList<>();
    /**
     * Configuration modifiers.
     */
    public final List<ConfigModifier<?>> configModifiers = new ArrayList<>();
    /**
     * Hooks.
     */
    public final List<GuiceyConfigurationHook> hooks = new ArrayList<>();
    /**
     * Setup objects.
     */
    public final List<TestEnvironmentSetup> extensions = new ArrayList<>();
    /**
     * Client factory.
     */
    public TestClientFactory clientFactory;
    /**
     * Inject test fields once.
     */
    public boolean injectOnce;
    /**
     * Service lookup for extensions enabled.
     */
    public boolean defaultExtensionsEnabled = true;
    /**
     * Extension registration source tracker (tracks source of registered setup objects).
     */
    public final TestExtensionsTracker tracker;

    /**
     * Reuse application instance between tests.
     */
    public boolean reuseApp;
    /**
     * Test class where reuse was declared.
     */
    public Class<?> reuseDeclarationClass;
    /**
     * Description of declaration field or annotation (in declaration class).
     */
    public String reuseSource;

    /**
     * Create config.
     *
     * @param tracker tracker
     */
    public ExtensionConfig(final TestExtensionsTracker tracker) {
        this.tracker = tracker;
    }


    /**
     * Register extensions declared in annotation.
     *
     * @param ann  annotation type
     * @param exts extensions
     */
    @SafeVarargs
    public final void extensionsFromAnnotation(final Class<? extends Annotation> ann,
                                               final Class<? extends TestEnvironmentSetup>... exts) {
        extensions.addAll(TestSetupUtils.create(exts));
        tracker.extensionsFromAnnotation(ann, exts);
    }

    /**
     * Register hooks declared in annotation.
     *
     * @param ann  annotation type
     * @param exts hooks
     */
    @SafeVarargs
    public final void hooksFromAnnotation(final Class<? extends Annotation> ann,
                                          final Class<? extends GuiceyConfigurationHook>... exts) {
        hooks.addAll(HooksUtil.create(exts));
        tracker.hooksFromAnnotation(ann, exts);
    }

    /**
     * Register hooks by instance (declared in field extension).
     *
     * @param exts hooks
     */
    public final void hookInstances(final GuiceyConfigurationHook... exts) {
        Collections.addAll(hooks, exts);
        tracker.hookInstances(exts);
    }

    /**
     * Register hook classes (declared in field extension).
     *
     * @param exts hooks
     */
    @SafeVarargs
    public final void hookClasses(final Class<? extends GuiceyConfigurationHook>... exts) {
        hooks.addAll(HooksUtil.create(exts));
        tracker.hookClasses(exts);
    }

    /**
     * Register configuration modifiers from annotation.
     *
     * @param ann       annotation type
     * @param modifiers modifiers
     */
    @SafeVarargs
    public final void configModifiersFromAnnotation(final Class<? extends Annotation> ann,
                                                    final Class<? extends ConfigModifier<?>>... modifiers) {
        configModifiers.addAll(ConfigOverrideUtils.createModifiers(modifiers));
        tracker.configModifiersFromAnnotation(ann, modifiers);
    }

    /**
     * Register configuration modifier classes (declared in field extension).
     *
     * @param modifiers modifiers
     */
    @SafeVarargs
    public final void configModifierClasses(final Class<? extends ConfigModifier<?>>... modifiers) {
        configModifiers.addAll(ConfigOverrideUtils.createModifiers(modifiers));
        tracker.configModifierClasses(modifiers);
    }

    /**
     * Register configuration modifiers declared as instances (in field extension).
     *
     * @param modifiers modifiers
     */
    public final void configModifierInstances(final ConfigModifier<?>... modifiers) {
        Collections.addAll(configModifiers, modifiers);
        tracker.configModifierInstances(modifiers);
    }

    /**
     * @param factoryType client factory class
     */
    @SuppressFBWarnings("PA_PUBLIC_PRIMITIVE_ATTRIBUTE")
    public final void clientFactory(final Class<? extends TestClientFactory> factoryType) {
        try {
            this.clientFactory = InstanceUtils.create(factoryType);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to instantiate test client factory", ex);
        }
    }

    /**
     * Obtain manual configuration instance with validation (a file should not be configured).
     *
     * @param configPath configuration file path (optional)
     * @param <C>        configuration type
     * @return manual configuration instance
     */
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
