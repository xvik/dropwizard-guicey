package ru.vyarus.dropwizard.guice.module.installer.feature;

import io.dropwizard.lifecycle.Managed;
import io.dropwizard.setup.Environment;
import ru.vyarus.dropwizard.guice.module.installer.FeatureInstaller;
import ru.vyarus.dropwizard.guice.module.installer.install.InstanceInstaller;
import ru.vyarus.dropwizard.guice.module.installer.order.Ordered;
import ru.vyarus.dropwizard.guice.module.installer.util.FeatureUtils;
import ru.vyarus.dropwizard.guice.module.installer.util.Reporter;

/**
 * Managed objects installer.
 * Looks for classes implementing {@code io.dropwizard.lifecycle.Managed} and register them in environment.
 */
public class ManagedInstaller implements
        FeatureInstaller<Managed>, InstanceInstaller<Managed>, Ordered {

    private final Reporter reporter = new Reporter(ManagedInstaller.class, "managed =");

    @Override
    public boolean matches(final Class<?> type) {
        return FeatureUtils.is(type, Managed.class);
    }

    @Override
    public void install(final Environment environment, final Managed instance) {
        reporter.line("(%s)", instance.getClass().getName());
        environment.lifecycle().manage(instance);
    }

    @Override
    public void report() {
        reporter.report();
    }
}
