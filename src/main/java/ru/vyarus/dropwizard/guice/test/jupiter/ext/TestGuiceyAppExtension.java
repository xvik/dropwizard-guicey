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
import ru.vyarus.dropwizard.guice.test.TestCommand;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;
import ru.vyarus.dropwizard.guice.test.util.ConfigOverrideUtils;
import ru.vyarus.dropwizard.guice.test.util.HooksUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@link TestGuiceyApp} junit 5 extension implementation. Normally, extension should be activated with annotation,
 * but in some cases manual registration may be used:
 * <pre>{@code
 * @RegisterExtension
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
 *     <{@code .hooks(builder -> builder.modules(new DebugGuiceModule()))}</li>
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
        // for usage with annotation
    }

    private TestGuiceyAppExtension(final Config config) {
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
    protected DropwizardTestSupport<?> prepareTestSupport(final ExtensionContext context) {
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
            config = Config.parse(ann);
        }

        HooksUtil.register(config.hooks);
        return create(config.app, config.configPath, ConfigOverrideUtils.convert(config.configOverrides));
    }

    @SuppressWarnings("unchecked")
    private <C extends Configuration> DropwizardTestSupport<C> create(
            final Class<? extends Application> app,
            final String configPath,
            final ConfigOverride... overrides) {
        return new DropwizardTestSupport<C>((Class<? extends Application<C>>) app,
                configPath,
                (String) null,
                TestCommand::new,
                overrides);
    }

    /**
     * Builder used for manual extension registration ({@link #forApp(Class)}).
     */
    public static class Builder {
        private final Config cfg = new Config();

        public Builder(final Class<? extends Application> app) {
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
         * Same as {@link TestGuiceyApp#configOverride()}. Multiple calls will not be merged!
         * <p>
         * WARNING: config override can't be used with parallel tests because all overriding values would be set as system
         * properties (see {@link io.dropwizard.testing.ConfigOverride#addToSystemProperties()}}).
         *
         * @param values overriding configuration values in "key: value" format
         * @return builder instance for chained calls
         */
        public Builder configOverrides(final String... values) {
            cfg.configOverrides = values;
            return this;
        }

        /**
         * Same as {@link TestGuiceyApp#hooks()}. May be called multiple times.
         *
         * @param hooks hook classes to use
         * @return builder instance for chained calls
         */
        public Builder hooks(final Class<? extends GuiceyConfigurationHook> hooks) {
            if (cfg.hooks == null) {
                cfg.hooks = HooksUtil.create(hooks);
            } else {
                cfg.hooks.addAll(HooksUtil.create(hooks));
            }
            return this;
        }

        /**
         * Has no annotation equivalent. May be used for quick configurations with lambda:
         * <pre>{@code
         * .hooks(builder -> builder.modules(new DebugModule()))
         * }</pre>
         * May be called multiple times.
         *
         * @param hooks hook instances (may be lambdas)
         * @return builder instance for chained calls
         */
        public Builder hooks(final GuiceyConfigurationHook... hooks) {
            if (cfg.hooks == null) {
                cfg.hooks = new ArrayList<>();
            }
            Collections.addAll(cfg.hooks, hooks);
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
    private static class Config {
        Class<? extends Application> app;
        String configPath = "";
        String[] configOverrides = new String[0];
        List<GuiceyConfigurationHook> hooks;

        /**
         * Converts annotation to unified configuration object.
         *
         * @param ann configuration annotation
         * @return configuration instance
         */
        static Config parse(final TestGuiceyApp ann) {
            final Config res = new Config();
            res.app = ann.value();
            res.configPath = ann.config();
            res.configOverrides = ann.configOverride();
            res.hooks = HooksUtil.create(ann.hooks());
            return res;
        }
    }
}
