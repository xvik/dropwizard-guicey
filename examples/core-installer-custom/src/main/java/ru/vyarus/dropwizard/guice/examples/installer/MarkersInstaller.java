package ru.vyarus.dropwizard.guice.examples.installer;

import io.dropwizard.core.setup.Environment;
import ru.vyarus.dropwizard.guice.debug.util.RenderUtils;
import ru.vyarus.dropwizard.guice.examples.service.MarkersCollector;
import ru.vyarus.dropwizard.guice.injector.lookup.InjectorLookup;
import ru.vyarus.dropwizard.guice.module.installer.FeatureInstaller;
import ru.vyarus.dropwizard.guice.module.installer.install.InstanceInstaller;
import ru.vyarus.dropwizard.guice.module.installer.util.FeatureUtils;
import ru.vyarus.dropwizard.guice.module.installer.util.Reporter;

/**
 * Installer recognize classes implementing {@link Marker} interface and sets guice-managed instance into
 * {@link MarkersCollector} service.
 * <p>
 * NOTE: this implementation is just an example of how to use gucie context within installer. Normally such cases
 * should be using guice multibindings for collecting "plugins" (and entire set injection in target service).
 *
 * @author Vyacheslav Rusakov
 * @since 29.01.2016
 */
public class MarkersInstaller implements FeatureInstaller, InstanceInstaller<Marker> {

    private final Reporter reporter = new Reporter(MarkersInstaller.class, "Installed markers = ");

    @Override
    public boolean matches(Class<?> type) {
        // recognize classes implementing interface
        return FeatureUtils.is(type, Marker.class);
    }

    @Override
    public void install(Environment environment, Marker instance) {
        // register instance in guice bean
        InjectorLookup.getInstance(environment, MarkersCollector.class).get().register(instance);
        // register instance for console report
        reporter.line(RenderUtils.renderClass(instance.getClass()));
    }

    @Override
    public void report() {
        // report all markers to console
        reporter.report();
    }
}
