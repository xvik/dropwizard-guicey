package ru.vyarus.dropwizard.guice.module.installer.feature;

import com.sun.jersey.spi.inject.InjectableProvider;
import io.dropwizard.setup.Environment;
import ru.vyarus.dropwizard.guice.module.installer.FeatureInstaller;
import ru.vyarus.dropwizard.guice.module.installer.install.InstanceInstaller;
import ru.vyarus.dropwizard.guice.module.installer.util.FeatureUtils;

/**
 * Jersey injectable provider installer.
 * Looks for classes implementing {@code com.sun.jersey.spi.inject.InjectableProvider} interface and register
 * in environment. Injectoble provider is always singleton no matter what is returned in getBean method.
 */
public class JerseyInjectableProviderInstaller implements FeatureInstaller<InjectableProvider>,
        InstanceInstaller<InjectableProvider> {

    @Override
    public boolean matches(final Class<?> type) {
        return FeatureUtils.is(type, InjectableProvider.class);
    }

    @Override
    public void install(final Environment environment, final InjectableProvider instance) {
        // register by instance to force singleton: prototype providers not allowed by jersey
        environment.jersey().register(instance);
    }
}
