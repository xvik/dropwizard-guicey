package ru.vyarus.dropwizard.guice.test.jupiter.ext;

import com.google.common.base.Preconditions;
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
import ru.vyarus.dropwizard.guice.module.installer.util.PathUtils;
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp;
import ru.vyarus.dropwizard.guice.test.util.ConfigOverrideUtils;
import ru.vyarus.dropwizard.guice.test.util.HooksUtil;

/**
 * {@link TestDropwizardApp} junit 5 extension implementation.
 *
 * @author Vyacheslav Rusakov
 * @since 28.04.2020
 */
public class DwAppJupiterExtension extends DwExtensionsSupport {

    public static final String STAR = "*";

    @Override
    protected DropwizardTestSupport<?> prepareTestSupport(final ExtensionContext context) {
        final TestDropwizardApp cfg = context.getElement().get().getAnnotation(TestDropwizardApp.class);
        // catch incorrect usage by direct @ExtendWith(...)
        Preconditions.checkNotNull(cfg, "%s annotation not declared: can't work without configuration, "
                        + "so either use annotation or extension with @%s for manual configuration",
                TestDropwizardApp.class.getSimpleName(),
                RegisterExtension.class.getSimpleName());

        HooksUtil.register(cfg.hooks());

        final DropwizardTestSupport support = new DropwizardTestSupport(cfg.value(),
                cfg.config(),
                buildConfigOverrides(cfg));

        if (cfg.randomPorts()) {
            support.addListener(new RandomPortsListener());
        }
        return support;
    }

    private ConfigOverride[] buildConfigOverrides(final TestDropwizardApp cfg) {
        ConfigOverride[] overrides = ConfigOverrideUtils.convert(cfg.configOverride());
        if (!Strings.isNullOrEmpty(cfg.restMapping())) {
            String mapping = PathUtils.leadingSlash(cfg.restMapping());
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
}
