package ru.vyarus.dropwizard.guice.test.spock.ext;

import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.DropwizardTestSupport;
import io.dropwizard.util.Strings;
import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook;
import ru.vyarus.dropwizard.guice.module.installer.util.PathUtils;
import ru.vyarus.dropwizard.guice.test.util.RandomPortsListener;
import ru.vyarus.dropwizard.guice.test.spock.UseDropwizardApp;
import ru.vyarus.dropwizard.guice.test.util.ConfigOverrideUtils;

/**
 * Spock extension for starting complete dropwizard app.
 *
 * @author Vyacheslav Rusakov
 * @since 03.01.2015
 */
public class DropwizardAppExtension extends AbstractAppExtension<UseDropwizardApp> {

    private static final String STAR = "*";

    @Override
    @SuppressWarnings("unchecked")
    protected GuiceyInterceptor.EnvironmentSupport buildSupport(final UseDropwizardApp annotation,
                                                                final Class<?> test) {
        return new GuiceyInterceptor.AbstractEnvironmentSupport(test) {
            @Override
            protected DropwizardTestSupport build() {
                final DropwizardTestSupport support = new DropwizardTestSupport(annotation.value(),
                        annotation.config(),
                        buildConfigOverrides(annotation));

                if (annotation.randomPorts()) {
                    support.addListener(new RandomPortsListener());
                }

                return support;
            }
        };
    }

    @Override
    protected Class<? extends GuiceyConfigurationHook>[] getHooks(final UseDropwizardApp annotation) {
        return annotation.hooks();
    }

    private ConfigOverride[] buildConfigOverrides(final UseDropwizardApp annotation) {
        ConfigOverride[] overrides = convertOverrides(annotation.configOverride());
        if (!Strings.isNullOrEmpty(annotation.restMapping())) {
            String mapping = PathUtils.leadingSlash(annotation.restMapping());
            if (!mapping.endsWith(STAR)) {
                mapping = PathUtils.trailingSlash(mapping) + STAR;
            }
            overrides = ConfigOverrideUtils.merge(overrides, ConfigOverride.config("server.rootPath", mapping));
        }
        return overrides;
    }
}
