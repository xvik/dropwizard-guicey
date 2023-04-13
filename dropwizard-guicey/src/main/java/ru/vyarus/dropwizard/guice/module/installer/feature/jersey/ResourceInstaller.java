package ru.vyarus.dropwizard.guice.module.installer.feature.jersey;


import com.google.common.base.Preconditions;
import com.google.inject.Binder;
import com.google.inject.Binding;
import com.google.inject.Injector;
import io.dropwizard.core.setup.Environment;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import ru.vyarus.dropwizard.guice.module.installer.install.TypeInstaller;
import ru.vyarus.dropwizard.guice.module.installer.install.binding.BindingInstaller;
import ru.vyarus.dropwizard.guice.module.installer.order.Order;
import ru.vyarus.dropwizard.guice.module.installer.util.BindingUtils;
import ru.vyarus.dropwizard.guice.module.installer.util.FeatureUtils;
import ru.vyarus.dropwizard.guice.module.installer.util.JerseyBinding;

import jakarta.ws.rs.Path;
import java.util.Arrays;
import java.util.List;

/**
 * Jersey resource installer.
 * Search classes annotated with {@link Path} or implementing interfaces annotated with {@link Path}
 * (only directly implemented). Directly register instance in jersey context to force singleton.
 * If we register it by type, then we could use prototype beans (resource instance created on each request),
 * which will lead to performance overhead. To ovoid misuse, singleton resources are forced, but
 * not for beans having explicit scope annotation.
 * See {@link ru.vyarus.dropwizard.guice.module.installer.InstallersOptions#ForceSingletonForJerseyExtensions}.
 * <p>
 * {@link ru.vyarus.dropwizard.guice.module.support.scope.Prototype} annotation may be used to force prototype
 * scope on guice beans (prevent forced singleton). This may be useful to avoid providers usage and directly
 * inject request, response and other request specific beans. Note that jersey managed resources may use direct
 * injections even in singletons (as jersey will use proxies instead of direct dependencies - implicit providers).
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
    public void bind(final Binder binder, final Class<?> type, final boolean lazyMarker) {
        final boolean jerseyManaged = isJerseyExtension(type);
        final boolean lazy = isLazy(type, lazyMarker);
        // register in guice only if not managed by hk and just in time (lazy) binding not requested
        if (!jerseyManaged && !lazy) {
            bindInGuice(binder, type);
        }
    }

    @Override
    public <T> void manualBinding(final Binder binder, final Class<T> type, final Binding<T> binding) {
        // no need to bind in case of manual binding
        final boolean hkManaged = isJerseyExtension(type);
        Preconditions.checkState(!hkManaged,
                // intentially no "at" before stacktrtace because idea may hide error in some cases
                "Resource annotated as jersey managed is declared manually in guice: %s (%s)",
                type.getName(), BindingUtils.getDeclarationSource(binding));
    }

    @Override
    public void install(final Environment environment, final Class<Object> type) {
        // type registration is required to properly start resource
        environment.jersey().register(type);
    }

    @Override
    public void install(final AbstractBinder binder, final Injector injector, final Class<Object> type) {
        final boolean jerseyManaged = isJerseyExtension(type);
        JerseyBinding.bindComponent(binder, injector, type, jerseyManaged, isForceSingleton(type, jerseyManaged));
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

    @Override
    public List<String> getRecognizableSigns() {
        return Arrays.asList("@" + Path.class.getSimpleName() + " on class",
                "@" + Path.class.getSimpleName() + " on implemented interface");
    }
}
