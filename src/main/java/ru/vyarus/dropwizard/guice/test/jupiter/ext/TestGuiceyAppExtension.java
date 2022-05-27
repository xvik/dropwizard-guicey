package ru.vyarus.dropwizard.guice.test.jupiter.ext;

import com.google.common.base.Preconditions;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.DropwizardTestSupport;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.platform.commons.support.AnnotationSupport;
import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook;
import ru.vyarus.dropwizard.guice.test.GuiceyTestSupport;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;
import ru.vyarus.dropwizard.guice.test.jupiter.env.TestEnvironmentSetup;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.conf.ExtensionBuilder;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.conf.ExtensionConfig;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.conf.ExtensionTracker;
import ru.vyarus.dropwizard.guice.test.util.ConfigOverrideUtils;
import ru.vyarus.dropwizard.guice.test.util.ConfigurablePrefix;
import ru.vyarus.dropwizard.guice.test.util.HooksUtil;
import ru.vyarus.dropwizard.guice.test.util.TestSetupUtils;

import java.util.Collections;
import java.util.List;

/**
 * {@link TestGuiceyApp} junit 5 extension implementation. Normally, extension should be activated with annotation,
 * but in some cases manual registration may be used:
 * <pre>{@code @RegisterExtension
 * static TestGuiceyAppExtension app = TestGuiceyAppExtension.forApp(MyApp.class).create()
 * }</pre>
 * This is complete equivalent of annotation declaration! Static modifier is important! There is no additional
 * methods in extension (intentionally), so registration type changes nothing in usage.
 * <p>
 * Reasons why it could be used instead of annotation:
 * <ul>
 *     <li>Incorrect execution order with some other extensions. Manually registered extension will execute
 *     after(!) all class level registrations (junit native behaviour). So moving guicey extension to manual
 *     registration may guarantee its execution after some other extension.</li>
 *     <li>Manual registration allows short hook declarations with lambdas:
 *     {@code .hooks(builder -> builder.modules(new DebugGuiceModule()))}</li>
 *     <li>Config overrides registration as {@link ConfigOverride} objects (required for delayed evaluated values:
 *     e.g. when it is obtained from some other junit extension)</li>
 * </ul>
 * <p>
 * You can't use manual registration to configure multiple applications because junit allows only one extension
 * instance (if you really need to use multiple applications in tests then register one with extension and for
 * another use {@link DropwizardTestSupport} directly).
 * <p>
 * If both declarations will be used at the same class (don't do that!) then annotation will win and manual
 * registration will be ignored (junit default behaviour).
 * <p>
 * Other extensions requiring access to dropwizard application may use
 * {@link GuiceyExtensionsSupport#lookupSupport(ExtensionContext)}.
 *
 * @author Vyacheslav Rusakov
 * @since 29.04.2020
 */
public class TestGuiceyAppExtension extends GuiceyExtensionsSupport {

    private Config config;

    public TestGuiceyAppExtension() {
        // extension created automatically by @TestGuiceyApp annotation
        super(new ExtensionTracker());
    }

    private TestGuiceyAppExtension(final Config config) {
        // manual extension creation from @RegisterExtension field
        super(config.tracker);
        this.config = config;
    }

    /**
     * Builder for manual extension registration with {@link RegisterExtension}. Provides the same configuration
     * options as {@link TestGuiceyApp} annotation (annotation considered as preferred usage way).
     * <p>
     * IMPORTANT: extension must be used with static field only! You can't register multiple extensions!
     * <p>
     * This is just a different way of extension configuration! Resulted extension object does not provide any
     * additional methods (and not intended to be used at all)!
     * <p>
     * Pure {@link DropwizardTestSupport} provides an ability to register custom {@link io.dropwizard.lifecycle.Managed}
     * or listener {@link DropwizardTestSupport#addListener(DropwizardTestSupport.ServiceListener)}. If you need these
     * then use {@link Builder#hooks(GuiceyConfigurationHook...)} to register additional managed object or
     * additional dropwizard bundle (which will be the same as listener above).
     *
     * @param app application class
     * @return builder for extension configuration.
     */
    public static Builder forApp(final Class<? extends Application> app) {
        return new Builder(app);
    }

    @Override
    protected DropwizardTestSupport<?> prepareTestSupport(final ExtensionContext context,
                                                          final List<TestEnvironmentSetup> setups) {
        if (config == null) {
            // Configure from annotation
            // Note that it is impossible to have both manually build config and annotation because annotation
            // will be processed first and manual registration will be simply ignored

            final TestGuiceyApp ann = AnnotationSupport
                    // also search annotation inside other annotations (meta)
                    .findAnnotation(context.getElement(), TestGuiceyApp.class).orElse(null);

            // catch incorrect usage by direct @ExtendWith(...)
            Preconditions.checkNotNull(ann, "%s annotation not declared: can't work without configuration, "
                            + "so either use annotation or extension with @%s for manual configuration",
                    TestGuiceyApp.class.getSimpleName(),
                    RegisterExtension.class.getSimpleName());
            config = Config.parse(ann, tracker);
        }

        // setups from @EnableSetup fields go last
        config.extensions.addAll(setups);
        TestSetupUtils.executeSetup(config, context);
        HooksUtil.register(config.hooks);

        return create(config.app, config.configPath, context);
    }

    @SuppressWarnings({"unchecked", "checkstyle:Indentation"})
    private <C extends Configuration> DropwizardTestSupport<C> create(
            final Class<? extends Application> app,
            final String configPath,
            final ExtensionContext context) {
        // config overrides work through system properties so it is important to have unique prefixes
        final String configPrefix = ConfigOverrideUtils.createPrefix(context.getRequiredTestClass());
        // NOTE: DropwizardTestSupport.ServiceListener listeners would be called ONLY on start!
        return new GuiceyTestSupport<C>((Class<? extends Application<C>>) app,
                configPath,
                configPrefix,
                buildConfigOverrides(configPrefix, context));
    }

    @SuppressWarnings("unchecked")
    private <T extends ConfigOverride & ConfigurablePrefix> ConfigOverride[] buildConfigOverrides(
            final String prefix, final ExtensionContext context) {
        final ConfigOverride[] overrides = ConfigOverrideUtils.convert(prefix, config.configOverrides);
        return config.configOverrideObjects.isEmpty() ? overrides
                : ConfigOverrideUtils.merge(overrides,
                ConfigOverrideUtils.prepareExtensionOverrides(
                        ConfigOverrideUtils.prepareOverrides(prefix, (List<T>) (List<?>) config.configOverrideObjects),
                        context
                ));
    }

    /**
     * Builder used for manual extension registration ({@link #forApp(Class)}).
     */
    public static class Builder extends ExtensionBuilder<Builder, Config> {

        public Builder(final Class<? extends Application> app) {
            super(new Config());
            this.cfg.app = Preconditions.checkNotNull(app, "Application class must be provided");
        }

        /**
         * Same as {@link TestGuiceyApp#config()}.
         *
         * @param configPath configuration file path
         * @return builder instance for chained calls
         */
        public Builder config(final String configPath) {
            cfg.configPath = configPath;
            return this;
        }

        /**
         * Environment support objects is the simplest mechanism to prepare additional objects for test
         * (like database) and apply configuration overrides. Provided classes would be instantiated with the
         * default constructor.
         * <p>
         * To avoid confusion with guicey hooks: setup object required to prepare test environment before test (and
         * apply required configurations) whereas hooks is a general mechanism for application customization (not only
         * in tests).
         * <p>
         * Anonymous implementation could be simply declared as static field:
         * {@code @EnableSupport static TestEnvironmentSupport ext = ext -> ext.configOverrides("foo:1")}
         * All such fields will be detected automatically and objects registered. Fields declared in base test classes
         * are also counted.
         *
         * @param support support object classes
         * @return builder instance for chained calls
         */
        @SafeVarargs
        public final Builder setup(final Class<? extends TestEnvironmentSetup>... support) {
            cfg.extensionClasses(support);
            return this;
        }

        /**
         * Environment support objects is the simplest mechanism to prepare additional objects for test
         * (like database) and apply configuration overrides.
         * <p>
         * To avoid confusion with guicey hooks: setup object required to prepare test environment before test (and
         * apply required configurations) whereas hooks is a general mechanism for application customization (not only
         * in tests).
         * <p>
         * Anonymous implementation could be simply declared as static field:
         * {@code @EnableSupport static TestEnvironmentSupport ext = ext -> ext.configOverrides("foo:1")}
         * All such fields will be detected automatically and objects registered. Fields declared in base test classes
         * are also counted.
         *
         * @param support support object instances
         * @return builder instance for chained calls
         */
        public Builder setup(final TestEnvironmentSetup... support) {
            cfg.extensionInstances(support);
            return this;
        }

        /**
         * Creates extension.
         * <p>
         * Note that extension must be assigned to static field! Extension instance does not provide additional
         * methods so use field and parameter injections as with annotation extension declaration.
         *
         * @return extension instance
         */
        public TestGuiceyAppExtension create() {
            return new TestGuiceyAppExtension(cfg);
        }
    }

    /**
     * Unified configuration.
     */
    @SuppressWarnings({"checkstyle:VisibilityModifier", "PMD.DefaultPackage"})
    private static class Config extends ExtensionConfig {
        Class<? extends Application> app;
        String configPath = "";

        Config() {
            super(new ExtensionTracker());
        }

        Config(final ExtensionTracker tracker) {
            super(tracker);
        }

        final void extensionInstances(final TestEnvironmentSetup... exts) {
            Collections.addAll(extensions, exts);
            tracker.extensionInstances(exts);
        }

        @SafeVarargs
        final void extensionClasses(final Class<? extends TestEnvironmentSetup>... exts) {
            extensions.addAll(TestSetupUtils.create(exts));
            tracker.extensionClasses(exts);
        }

        /**
         * Converts annotation to unified configuration object.
         *
         * @param ann configuration annotation
         * @return configuration instance
         */
        static Config parse(final TestGuiceyApp ann, final ExtensionTracker tracker) {
            final Config res = new Config(tracker);
            res.app = ann.value();
            res.configPath = ann.config();
            res.configOverrides = ann.configOverride();
            res.hooksFromAnnotation(ann.annotationType(), ann.hooks());
            res.extensionsFromAnnotation(ann.annotationType(), ann.setup());
            return res;
        }
    }
}
