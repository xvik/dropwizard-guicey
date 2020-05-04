package ru.vyarus.dropwizard.guice.test.jupiter.ext;

import com.google.common.base.Preconditions;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.jetty.HttpConnectorFactory;
import io.dropwizard.server.DefaultServerFactory;
import io.dropwizard.server.ServerFactory;
import io.dropwizard.server.SimpleServerFactory;
import io.dropwizard.setup.Environment;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.DropwizardTestSupport;
import io.dropwizard.util.Strings;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.platform.commons.support.AnnotationSupport;
import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook;
import ru.vyarus.dropwizard.guice.module.installer.util.PathUtils;
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp;
import ru.vyarus.dropwizard.guice.test.util.ConfigOverrideUtils;
import ru.vyarus.dropwizard.guice.test.util.HooksUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@link TestDropwizardApp} junit 5 extension implementation. Normally, extension should be activated with annotation,
 * but in some cases manual registration may be used:
 * <pre>{@code
 * @RegisterExtension
 * static TestDropwizardAppExtension app = TestDropwizardAppExtension.forApp(MyApp.class).create()
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
 * {@link GuiceyExtensionsSupport#lookup(ExtensionContext)}.
 *
 * @author Vyacheslav Rusakov
 * @since 28.04.2020
 */
public class TestDropwizardAppExtension extends GuiceyExtensionsSupport {

    private static final String STAR = "*";

    private Config config;

    public TestDropwizardAppExtension() {
        // for usage with annotation
    }

    private TestDropwizardAppExtension(final Config config) {
        this.config = config;
    }

    /**
     * Builder for manual extension registration with {@link RegisterExtension}. Provides the same configuration
     * options as {@link TestDropwizardApp} annotation (annotation considered as preferred usage way).
     * <p>
     * IMPORTANT: extension must be used with static field only! You can't register multiple extensions!
     * <p>
     * This is just a different way of extension configuration! Resulted extension object does not provide any
     * additional methods (and not intended to be used at all)!
     * <p>
     * Pure {@link DropwizardTestSupport} provides an ability to register custom {@link io.dropwizard.lifecycle.Managed}
     * or listener {@link DropwizardTestSupport#addListener(DropwizardTestSupport.ServiceListener)}. If you need these
     * then use {@link Builder#hooks(GuiceyConfigurationHook...)} to register additional managed object or
     * additional dropwizard or guicey bundle (which will be the same as listener above).
     *
     * @param app application class
     * @return builder for extension configuration.
     */
    public static Builder forApp(final Class<? extends Application> app) {
        return new Builder(app);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected DropwizardTestSupport<?> prepareTestSupport(final ExtensionContext context) {
        if (config == null) {
            // Configure from annotation
            // Note that it is impossible to have both manually build config and annotation because annotation
            // will be processed first and manual registration will be simply ignored

            final TestDropwizardApp ann = AnnotationSupport
                    // also search annotation inside other annotations (meta)
                    .findAnnotation(context.getElement(), TestDropwizardApp.class).orElse(null);

            // catch incorrect usage by direct @ExtendWith(...)
            Preconditions.checkNotNull(ann, "%s annotation not declared: can't work without configuration, "
                            + "so either use annotation or extension with @%s for manual configuration",
                    TestDropwizardApp.class.getSimpleName(),
                    RegisterExtension.class.getSimpleName());

            config = Config.parse(ann);
        }

        HooksUtil.register(config.hooks);

        final DropwizardTestSupport support = new DropwizardTestSupport(config.app,
                config.configPath,
                buildConfigOverrides());

        if (config.randomPorts) {
            support.addListener(new RandomPortsListener());
        }
        return support;
    }

    private ConfigOverride[] buildConfigOverrides() {
        ConfigOverride[] overrides = ConfigOverrideUtils.convert(config.configOverrides);
        if (!Strings.isNullOrEmpty(config.restMapping)) {
            String mapping = PathUtils.leadingSlash(config.restMapping);
            if (!mapping.endsWith(STAR)) {
                mapping = PathUtils.trailingSlash(mapping) + STAR;
            }
            overrides = ConfigOverrideUtils.merge(overrides, ConfigOverride.config("server.rootPath", mapping));
        }
        return overrides;
    }

    /**
     * Applies random ports to test application.
     */
    public static class RandomPortsListener extends DropwizardTestSupport.ServiceListener<Configuration> {
        @Override
        public void onRun(final Configuration configuration,
                          final Environment environment,
                          final DropwizardTestSupport<Configuration> rule) throws Exception {
            final ServerFactory server = configuration.getServerFactory();
            if (server instanceof SimpleServerFactory) {
                ((HttpConnectorFactory) ((SimpleServerFactory) server).getConnector()).setPort(0);
            } else {
                final DefaultServerFactory dserv = (DefaultServerFactory) server;
                ((HttpConnectorFactory) dserv.getApplicationConnectors().get(0)).setPort(0);
                ((HttpConnectorFactory) dserv.getAdminConnectors().get(0)).setPort(0);
            }
        }
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
         * Same as {@link TestDropwizardApp#config()}.
         *
         * @param configPath configuration file path
         * @return builder instance for chained calls
         */
        public Builder config(final String configPath) {
            cfg.configPath = configPath;
            return this;
        }

        /**
         * Same as {@link TestDropwizardApp#configOverride()}. Multiple calls will not be merged!
         *
         * @param values overriding configuration values
         * @return builder instance for chained calls
         */
        public Builder configOverrides(final String... values) {
            cfg.configOverrides = values;
            return this;
        }

        /**
         * Same as {@link TestDropwizardApp#hooks()}. May be called multiple times.
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
         * Same as {@link TestDropwizardApp#randomPorts()}.
         *
         * @param randomPorts true to use random ports
         * @return builder instance for chained calls
         */
        public Builder randomPorts(final boolean randomPorts) {
            cfg.randomPorts = randomPorts;
            return this;
        }

        /**
         * Shortcut for {@link #randomPorts(boolean)}.
         *
         * @return builder instance for chained calls
         */
        public Builder randomPorts() {
            return randomPorts(true);
        }

        /**
         * Same as {@link TestDropwizardApp#restMapping()}.
         *
         * @param mapping rest mapping path
         * @return builder instance for chained calls
         */
        public Builder restMapping(final String mapping) {
            cfg.restMapping = mapping;
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
        public TestDropwizardAppExtension create() {
            return new TestDropwizardAppExtension(cfg);
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
        boolean randomPorts;
        String restMapping = "";

        /**
         * Converts annotation to unified configuration object.
         *
         * @param ann configuration annotation
         * @return configuration instance
         */
        static Config parse(final TestDropwizardApp ann) {
            final Config res = new Config();
            res.app = ann.value();
            res.configPath = ann.config();
            res.configOverrides = ann.configOverride();
            res.hooks = HooksUtil.create(ann.hooks());
            res.randomPorts = ann.randomPorts();
            res.restMapping = ann.restMapping();
            return res;
        }
    }
}
