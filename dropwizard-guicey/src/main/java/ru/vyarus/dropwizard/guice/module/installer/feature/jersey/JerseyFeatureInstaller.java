package ru.vyarus.dropwizard.guice.module.installer.feature.jersey;

import io.dropwizard.setup.Environment;
import ru.vyarus.dropwizard.guice.debug.util.RenderUtils;
import ru.vyarus.dropwizard.guice.module.installer.FeatureInstaller;
import ru.vyarus.dropwizard.guice.module.installer.install.InstanceInstaller;
import ru.vyarus.dropwizard.guice.module.installer.order.Order;
import ru.vyarus.dropwizard.guice.module.installer.util.FeatureUtils;
import ru.vyarus.dropwizard.guice.module.installer.util.Reporter;

import javax.ws.rs.core.Feature;
import java.util.Collections;
import java.util.List;

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
public class JerseyFeatureInstaller implements FeatureInstaller, InstanceInstaller<Feature> {

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
        reporter.line(RenderUtils.renderClassLine(FeatureUtils.getInstanceClass(instance)));
        environment.jersey().register(instance);
    }

    @Override
    public List<String> getRecognizableSigns() {
        return Collections.singletonList("implements " + Feature.class.getSimpleName());
    }
}
