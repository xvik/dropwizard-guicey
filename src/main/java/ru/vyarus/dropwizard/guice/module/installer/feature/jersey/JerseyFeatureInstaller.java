package ru.vyarus.dropwizard.guice.module.installer.feature.jersey;

import io.dropwizard.setup.Environment;
import ru.vyarus.dropwizard.guice.module.installer.FeatureInstaller;
import ru.vyarus.dropwizard.guice.module.installer.install.InstanceInstaller;
import ru.vyarus.dropwizard.guice.module.installer.order.Order;
import ru.vyarus.dropwizard.guice.module.installer.util.FeatureUtils;
import ru.vyarus.dropwizard.guice.module.installer.util.Reporter;

import javax.ws.rs.core.Feature;

/**
 * Jersey feature installer.
 * Search classes implementing {@link Feature}. Directly register instance in jersey context.
 * <p>
 * Installer is useful when guice-managed component required for configuration.
 *
 * @author Vyacheslav Rusakov
 * @since 13.01.2016
 */
@Order(30)
public class JerseyFeatureInstaller implements
        FeatureInstaller<Feature>, InstanceInstaller<Feature> {

    private final Reporter reporter = new Reporter(JerseyFeatureInstaller.class, "features =");

    @Override
    public boolean matches(final Class<?> type) {
        return FeatureUtils.is(type, Feature.class);
    }

    @Override
    public void report() {
        reporter.report();
    }

    @Override
    public void install(final Environment environment, final Feature instance) {
        reporter.line("(%s)", FeatureUtils.getInstanceClass(instance).getName());
        environment.jersey().register(instance);
    }
}
