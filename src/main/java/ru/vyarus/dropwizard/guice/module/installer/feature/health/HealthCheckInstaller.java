package ru.vyarus.dropwizard.guice.module.installer.feature.health;

import io.dropwizard.setup.Environment;
import ru.vyarus.dropwizard.guice.debug.util.RenderUtils;
import ru.vyarus.dropwizard.guice.module.installer.FeatureInstaller;
import ru.vyarus.dropwizard.guice.module.installer.install.InstanceInstaller;
import ru.vyarus.dropwizard.guice.module.installer.order.Order;
import ru.vyarus.dropwizard.guice.module.installer.util.FeatureUtils;
import ru.vyarus.dropwizard.guice.module.installer.util.Reporter;

import java.util.Collections;
import java.util.List;

/**
 * Health check installer.
 * Looks for classes extending
 * {@code ru.vyarus.dropwizard.guice.module.installer.installer.health.NamedHealthCheck} and register in
 * environment.
 *
 * @author Vyacheslav Rusakov
 * @since 01.09.2014
 */
@Order(60)
public class HealthCheckInstaller implements FeatureInstaller, InstanceInstaller<NamedHealthCheck> {

    private final Reporter reporter = new Reporter(HealthCheckInstaller.class, "health checks =");

    @Override
    public boolean matches(final Class<?> type) {
        return FeatureUtils.is(type, NamedHealthCheck.class);
    }

    @Override
    public void install(final Environment environment, final NamedHealthCheck instance) {
        environment.healthChecks().register(instance.getName(), instance);
        reporter.line("%-20s %s", instance.getName(),
                RenderUtils.renderClassLine(FeatureUtils.getInstanceClass(instance)));
    }

    @Override
    public void report() {
        reporter.report();
    }

    @Override
    public List<String> getRecognizableSigns() {
        return Collections.singletonList("extends " + NamedHealthCheck.class.getSimpleName());
    }
}
