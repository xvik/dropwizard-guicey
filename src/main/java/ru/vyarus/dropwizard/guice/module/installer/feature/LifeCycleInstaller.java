package ru.vyarus.dropwizard.guice.module.installer.feature;

import io.dropwizard.setup.Environment;
import org.eclipse.jetty.util.component.LifeCycle;
import ru.vyarus.dropwizard.guice.module.installer.FeatureInstaller;
import ru.vyarus.dropwizard.guice.module.installer.util.FeatureUtils;

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
