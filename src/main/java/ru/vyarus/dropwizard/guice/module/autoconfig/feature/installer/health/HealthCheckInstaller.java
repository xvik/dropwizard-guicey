package ru.vyarus.dropwizard.guice.module.autoconfig.feature.installer.health;

import com.codahale.metrics.health.HealthCheck;
import io.dropwizard.setup.Environment;
import ru.vyarus.dropwizard.guice.module.autoconfig.feature.FeatureInstaller;
import ru.vyarus.dropwizard.guice.module.autoconfig.util.FeatureUtils;

/**
 * Health check installer.
 * Looks for classes extending
 * {@code ru.vyarus.dropwizard.guice.module.autoconfig.feature.installer.health.NamedHealthCheck} and register in
 * environment.
 */
public class HealthCheckInstaller implements FeatureInstaller<NamedHealthCheck> {

    @Override
    public boolean matches(final Class<?> type) {
        return FeatureUtils.is(type, HealthCheck.class);
    }

    @Override
    public void install(final Environment environment, final NamedHealthCheck instance) {
        environment.healthChecks().register(instance.getName(), instance);
    }
}
