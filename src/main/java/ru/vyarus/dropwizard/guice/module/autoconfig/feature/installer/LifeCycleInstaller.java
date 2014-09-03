package ru.vyarus.dropwizard.guice.module.autoconfig.feature.installer;

import io.dropwizard.setup.Environment;
import org.eclipse.jetty.util.component.LifeCycle;
import ru.vyarus.dropwizard.guice.module.autoconfig.feature.FeatureInstaller;
import ru.vyarus.dropwizard.guice.module.autoconfig.util.FeatureUtils;

/**
 * Lifecycle objects installer.
 * Looks for classes implementing {@code org.eclipse.jetty.util.component.LifeCycle} and register them in environment.
 */
public class LifeCycleInstaller implements FeatureInstaller<LifeCycle> {

    @Override
    public boolean matches(final Class<?> type) {
        return FeatureUtils.is(type, LifeCycle.class);
    }

    @Override
    public void install(final Environment environment, final LifeCycle instance) {
        environment.lifecycle().manage(instance);
    }
}
