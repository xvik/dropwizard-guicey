package ru.vyarus.dropwizard.guice.module.installer.feature.jersey;


import com.google.inject.Binder;
import com.google.inject.Injector;
import io.dropwizard.setup.Environment;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import ru.vyarus.dropwizard.guice.module.installer.FeatureInstaller;
import ru.vyarus.dropwizard.guice.module.installer.install.JerseyInstaller;
import ru.vyarus.dropwizard.guice.module.installer.install.TypeInstaller;
import ru.vyarus.dropwizard.guice.module.installer.install.binding.BindingInstaller;
import ru.vyarus.dropwizard.guice.module.installer.order.Order;
import ru.vyarus.dropwizard.guice.module.installer.util.FeatureUtils;
import ru.vyarus.dropwizard.guice.module.installer.util.JerseyBinding;

import javax.inject.Singleton;
import javax.ws.rs.Path;

import static ru.vyarus.dropwizard.guice.module.installer.util.JerseyBinding.isHK2Managed;

/**
 * Jersey resource installer.
 * Search classes annotated with {@link Path}. Directly register instance in jersey context to force singleton.
 * If we register it by type, then we could use prototype beans (resource instance created on each request),
 * which will lead to performance overhead. To ovoid misuse, singleton resources are forced. Override installer
 * if you really need prototype resources.
 *
 * @author Vyacheslav Rusakov
 * @since 01.09.2014
 */
@Order(40)
public class ResourceInstaller implements FeatureInstaller<Object>, BindingInstaller, TypeInstaller<Object>,
        JerseyInstaller<Object> {

    @Override
    public boolean matches(final Class<?> type) {
        return FeatureUtils.hasAnnotation(type, Path.class);
    }

    @Override
    public <T> void install(final Binder binder, final Class<? extends T> type, final boolean lazy) {
        if (!isHK2Managed(type) && !lazy) {
            // force singleton
            binder.bind(type).in(Singleton.class);
        }
    }

    @Override
    public void install(final Environment environment, final Class<Object> type) {
        // type registration is required to properly start resource
        environment.jersey().register(type);
    }

    @Override
    public void install(final AbstractBinder binder, final Injector injector, final Class<Object> type) {
        JerseyBinding.bindComponent(binder, injector, type);
    }

    @Override
    public void report() {
        // dropwizard logs installed resources
    }
}
