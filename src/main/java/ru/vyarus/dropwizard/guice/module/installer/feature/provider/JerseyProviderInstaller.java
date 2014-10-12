package ru.vyarus.dropwizard.guice.module.installer.feature.provider;

import com.google.inject.Binder;
import io.dropwizard.setup.Environment;
import ru.vyarus.dropwizard.guice.module.installer.FeatureInstaller;
import ru.vyarus.dropwizard.guice.module.installer.install.BindingInstaller;
import ru.vyarus.dropwizard.guice.module.installer.install.TypeInstaller;
import ru.vyarus.dropwizard.guice.module.installer.util.FeatureUtils;

import javax.inject.Singleton;
import javax.ws.rs.ext.Provider;

/**
 * Jersey provider installer.
 * Looks for classes annotated with {@code @javax.ws.rs.ext.Provider} and register in environment.
 */
public class JerseyProviderInstaller implements FeatureInstaller<Object>,
        BindingInstaller, TypeInstaller<Object> {
    private final ProviderReporter reporter = new ProviderReporter();

    @Override
    public boolean matches(final Class<?> type) {
        return FeatureUtils.hasAnnotation(type, Provider.class);
    }

    @Override
    public <T> void install(final Binder binder, final Class<? extends T> type) {
        // force singleton
        binder.bind(type).in(Singleton.class);
    }

    @Override
    public void install(final Environment environment, final Class<Object> type) {
        reporter.provider(type);
        environment.jersey().register(type);
    }

    @Override
    public void report() {
        reporter.report();
    }
}
