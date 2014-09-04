package ru.vyarus.dropwizard.guice.module.installer.feature;

import io.dropwizard.lifecycle.Managed;
import io.dropwizard.setup.Environment;
import ru.vyarus.dropwizard.guice.module.installer.FeatureInstaller;
import ru.vyarus.dropwizard.guice.module.installer.util.FeatureUtils;

/**
 * Managed objects installer.
 * Looks for classes implementing {@code io.dropwizard.lifecycle.Managed} and register them in environment.
 */
public class ManagedInstaller implements FeatureInstaller<Managed> {
    @Override
    public boolean matches(final Class<?> type) {
        return FeatureUtils.is(type, Managed.class);
    }

    @Override
    public void install(final Environment environment, final Managed instance) {
        environment.lifecycle().manage(instance);
    }
}
