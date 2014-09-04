package ru.vyarus.dropwizard.guice.module.installer.feature;

import com.sun.jersey.spi.inject.InjectableProvider;
import io.dropwizard.setup.Environment;
import ru.vyarus.dropwizard.guice.module.installer.FeatureInstaller;
import ru.vyarus.dropwizard.guice.module.installer.util.FeatureUtils;

/**
 * Jersey injectable provider installer.
 * Looks for classes implementing {@code com.sun.jersey.spi.inject.InjectableProvider} interface and register
 * in environment.
 */
public class JerseyInjectableProviderInstaller implements FeatureInstaller<InjectableProvider> {

    @Override
    public boolean matches(final Class<?> type) {
        return FeatureUtils.is(type, InjectableProvider.class);
    }

    @Override
    public void install(final Environment environment, final InjectableProvider instance) {
        environment.jersey().register(instance);
    }
}
