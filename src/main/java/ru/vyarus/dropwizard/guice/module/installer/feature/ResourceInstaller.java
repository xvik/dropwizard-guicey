package ru.vyarus.dropwizard.guice.module.installer.feature;


import com.google.inject.Binder;
import ru.vyarus.dropwizard.guice.module.installer.FeatureInstaller;
import ru.vyarus.dropwizard.guice.module.installer.install.BindingInstaller;
import ru.vyarus.dropwizard.guice.module.installer.util.FeatureUtils;

import javax.inject.Singleton;
import javax.ws.rs.Path;

/**
 * Jersey resource installer.
 * Search classes annotated with {@code @Path}. Directly register instance in jersey context to force singleton.
 * If we register it by type, then we could use prototype beans (resource instance created on each request),
 * which will lead to performance overhead. To ovoid misuse, singleton resources are forced. Override installer
 * if you really need prototype resources.
 */
public class ResourceInstaller implements FeatureInstaller<Object>, BindingInstaller {

    @Override
    public boolean matches(final Class<?> type) {
        return FeatureUtils.hasAnnotation(type, Path.class);
    }

    @Override
    public <T> void install(final Binder binder, final Class<? extends T> type) {
        // force singleton
        binder.bind(type).in(Singleton.class);
    }

    @Override
    public void report() {
        // dropwizard logs installed resources
    }
}
