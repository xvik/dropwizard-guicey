package ru.vyarus.dropwizard.guice.module.installer.feature.jersey;


import com.google.inject.Binder;
import com.google.inject.Injector;
import io.dropwizard.setup.Environment;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import ru.vyarus.dropwizard.guice.module.installer.install.TypeInstaller;
import ru.vyarus.dropwizard.guice.module.installer.install.binding.BindingInstaller;
import ru.vyarus.dropwizard.guice.module.installer.order.Order;
import ru.vyarus.dropwizard.guice.module.installer.util.FeatureUtils;
import ru.vyarus.dropwizard.guice.module.installer.util.JerseyBinding;

import javax.ws.rs.Path;

/**
 * Jersey resource installer.
 * Search classes annotated with {@link Path} or implementing interfaces annotated with {@link Path}
 * (only directly implemented). Directly register instance in jersey context to force singleton.
 * If we register it by type, then we could use prototype beans (resource instance created on each request),
 * which will lead to performance overhead. To ovoid misuse, singleton resources are forced, but
 * not for beans having explicit scope annotation.
 * See {@link ru.vyarus.dropwizard.guice.module.installer.InstallersOptions#ForceSingletonForHkExtensions}.
 *
 * @author Vyacheslav Rusakov
 * @since 01.09.2014
 */
@Order(40)
public class ResourceInstaller extends AbstractJerseyInstaller<Object> implements
        BindingInstaller,
        TypeInstaller<Object> {

    @Override
    public boolean matches(final Class<?> type) {
        return !type.isInterface()
                && (FeatureUtils.hasAnnotation(type, Path.class) || hasMatchedInterfaces(type));
    }

    @Override
    public <T> void install(final Binder binder, final Class<? extends T> type, final boolean lazyMarker) {
        final boolean hkManaged = isHkExtension(type);
        final boolean lazy = isLazy(type, lazyMarker);
        // register in guice only if not managed by hk and just in time (lazy) binding not requested
        if (!hkManaged && !lazy) {
            bindInGuice(binder, type);
        }
    }

    @Override
    public void install(final Environment environment, final Class<Object> type) {
        // type registration is required to properly start resource
        environment.jersey().register(type);
    }

    @Override
    public void install(final AbstractBinder binder, final Injector injector, final Class<Object> type) {
        final boolean hkManaged = isHkExtension(type);
        JerseyBinding.bindComponent(binder, injector, type, hkManaged, isForceSingleton(type, hkManaged));
    }

    @Override
    public void report() {
        // dropwizard logs installed resources
    }

    private boolean hasMatchedInterfaces(final Class<?> type) {
        boolean matches = false;
        // looking only first interface level
        for (Class<?> iface : type.getInterfaces()) {
            if (iface.isAnnotationPresent(Path.class)) {
                matches = true;
                break;
            }
        }
        return matches;
    }
}
