package ru.vyarus.dropwizard.guice.module.installer.feature.health;

import com.codahale.metrics.health.HealthCheck;
import io.dropwizard.setup.Environment;
import ru.vyarus.dropwizard.guice.module.installer.FeatureInstaller;
import ru.vyarus.dropwizard.guice.module.installer.install.InstanceInstaller;
import ru.vyarus.dropwizard.guice.module.installer.util.FeatureUtils;
import ru.vyarus.dropwizard.guice.module.installer.util.Reporter;

/**
 * Health check installer.
 * Looks for classes extending
 * {@code ru.vyarus.dropwizard.guice.module.installer.installer.health.NamedHealthCheck} and register in
 * environment.
 */
public class HealthCheckInstaller implements FeatureInstaller<NamedHealthCheck>,
        InstanceInstaller<NamedHealthCheck> {

    private final Reporter reporter = new Reporter(HealthCheckInstaller.class, "health checks =");

    @Override
    public boolean matches(final Class<?> type) {
        return FeatureUtils.is(type, HealthCheck.class);
    }

    @Override
    public void install(final Environment environment, final NamedHealthCheck instance) {
        environment.healthChecks().register(instance.getName(), instance);
        reporter.line("%-10s (%s)", instance.getName(), FeatureUtils.getInstanceClass(instance).getName());
    }

    @Override
    public void report() {
        reporter.report();
    }
}
