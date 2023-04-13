package ru.vyarus.dropwizard.guice.module.installer.feature.jersey.provider;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.inject.Binder;
import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Stage;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.internal.inject.InjectionResolver;
import org.glassfish.jersey.server.model.ModelProcessor;
import org.glassfish.jersey.server.monitoring.ApplicationEventListener;
import org.glassfish.jersey.server.spi.internal.ValueParamProvider;
import ru.vyarus.dropwizard.guice.module.installer.InstallersOptions;
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.AbstractJerseyInstaller;
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.JerseyManaged;
import ru.vyarus.dropwizard.guice.module.installer.install.binding.BindingInstaller;
import ru.vyarus.dropwizard.guice.module.installer.order.Order;
import ru.vyarus.dropwizard.guice.module.installer.util.BindingUtils;
import ru.vyarus.dropwizard.guice.module.installer.util.FeatureUtils;
import ru.vyarus.java.generics.resolver.GenericsResolver;

import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.DynamicFeature;
import jakarta.ws.rs.ext.*;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import static ru.vyarus.dropwizard.guice.module.installer.util.FeatureUtils.is;
import static ru.vyarus.dropwizard.guice.module.installer.util.JerseyBinding.*;

/**
 * Jersey provider installer.
 * Looks for jersey extension classes and classes annotated with {@link jakarta.ws.rs.ext.Provider} and register
 * bindings in HK context.
 * <p>
 * Registration by extension type might be disabled using
 * {@link ru.vyarus.dropwizard.guice.module.installer.InstallersOptions#JerseyExtensionsRecognizedByType} option
 * (for legacy behaviour - register classed only annotated with {@link jakarta.ws.rs.ext.Provider}).
 * <p>
 * By default, user providers are prioritized (with {@link org.glassfish.jersey.internal.inject.Custom}
 * qualifier). This is the default dropwizard behaviour for direct provider registration with
 * {@code environment.jersey().register(provider)} and so installer behaves the same. Without it ambiguous situations
 * are possible when dropwizard default providers used instead (e.g. user provided {@code ExceptionMapper<Throwable>}
 * not used at all because of dropwizard's one).
 * Auto qualification may be disabled with
 * {@link ru.vyarus.dropwizard.guice.module.installer.InstallersOptions#PrioritizeJerseyExtensions} (to mimic older
 * guicey versions behaviour). When auto prioritization disabled, {@link org.glassfish.jersey.internal.inject.Custom}
 * annotation may be used directly (to prioritize exact providers).
 * <p>
 * {@link jakarta.annotation.Priority} may be used to order providers (see {@link jakarta.ws.rs.Priorities} for
 * the default priority constants).
 * <p>
 * If provider is annotated with {@link JerseyManaged} it's instance will be created by HK2, not guice.
 * This is important when extensions directly depends on HK beans (no way to wrap with {@link Provider}
 * or if it's eager extension, which instantiated by HK immediately (when hk-guice contexts not linked yet).
 * <p>
 * In some cases {@link ru.vyarus.dropwizard.guice.module.installer.install.binding.LazyBinding} could
 * be an alternative to {@link JerseyManaged}
 * <p>
 * Force singleton scope for extensions, but not for beans having explicit scope annotation.
 * See {@link ru.vyarus.dropwizard.guice.module.installer.InstallersOptions#ForceSingletonForJerseyExtensions}.
 * {@link ru.vyarus.dropwizard.guice.module.support.scope.Prototype} annotation may be used on guice beans
 * to declare bean in prototype scope (prevent forced singleton).
 *
 * @author Vyacheslav Rusakov
 * @see JerseyManaged
 * @see ru.vyarus.dropwizard.guice.module.installer.feature.jersey.GuiceManaged
 * @see ru.vyarus.dropwizard.guice.module.installer.install.binding.LazyBinding
 * @since 10.10.2014
 */
@SuppressWarnings("PMD.ExcessiveImports")
@Order(30)
public class JerseyProviderInstaller extends AbstractJerseyInstaller<Object> implements
        BindingInstaller {

    private static final Set<Class<?>> EXTENSION_TYPES = ImmutableSet.<Class<?>>of(
            ExceptionMapper.class,
            ParamConverterProvider.class,
            ContextResolver.class,
            MessageBodyReader.class,
            MessageBodyWriter.class,
            ReaderInterceptor.class,
            WriterInterceptor.class,
            ContainerRequestFilter.class,
            ContainerResponseFilter.class,
            DynamicFeature.class,
            ValueParamProvider.class,
            InjectionResolver.class,
            ApplicationEventListener.class,
            ModelProcessor.class
    );

    private final ProviderReporter reporter = new ProviderReporter();

    @Override
    public boolean matches(final Class<?> type) {
        return FeatureUtils.hasAnnotation(type, Provider.class)
                || (!Modifier.isAbstract(type.getModifiers())
                && (boolean) option(InstallersOptions.JerseyExtensionsRecognizedByType)
                && EXTENSION_TYPES.stream().anyMatch(ext -> ext.isAssignableFrom(type)));
    }

    @Override
    public void bind(final Binder binder, final Class<?> type, final boolean lazyMarker) {
        final boolean hkManaged = isJerseyExtension(type);
        final boolean lazy = isLazy(type, lazyMarker);
        // register in guice only if not managed by hk and just in time (lazy) binding not requested
        if (!hkManaged && !lazy) {
            bindInGuice(binder, type);
        }
    }

    @Override
    public <T> void manualBinding(final Binder binder, final Class<T> type, final Binding<T> binding) {
        // no need to bind in case of manual binding
        final boolean hkManaged = isJerseyExtension(type);
        Preconditions.checkState(!hkManaged,
                // intentially no "at" before stacktrtace because idea may hide error in some cases
                "Provider annotated as jersey managed is declared manually in guice: %s (%s)",
                type.getName(), BindingUtils.getDeclarationSource(binding));
    }

    @Override
    public void extensionBound(final Stage stage, final Class<?> type) {
        if (stage != Stage.TOOL) {
            // reporting (common for both registration types)
            final boolean hkManaged = isJerseyExtension(type);
            reporter.provider(type, hkManaged, false);
        }
    }

    @Override
    public void install(final AbstractBinder binder, final Injector injector, final Class<Object> type) {
        final boolean hkExtension = isJerseyExtension(type);
        final boolean forceSingleton = isForceSingleton(type, hkExtension);
        final boolean prioritize = option(InstallersOptions.PrioritizeJerseyExtensions);
        // since jersey 2.26 internal hk Factory class replaced by java 8 Supplier
        if (is(type, Supplier.class)) {
            // register factory directly (without wrapping)
            bindFactory(binder, injector, type, hkExtension, forceSingleton);

        } else {
            // support multiple extension interfaces on one type
            final Set<Class<?>> extensions = Sets.intersection(EXTENSION_TYPES,
                    GenericsResolver.resolve(type).getGenericsInfo().getComposingTypes());
            if (!extensions.isEmpty()) {
                for (Class<?> ext : extensions) {
                    bindSpecificComponent(binder, injector, type, ext, hkExtension, forceSingleton, prioritize,
                            // model processor must be bound by instance (initialization specific)
                            ModelProcessor.class.equals(ext));
                }
            } else {
                // no known extension found
                bindComponent(binder, injector, type, hkExtension, forceSingleton);
            }
        }
    }

    @Override
    public void report() {
        reporter.report();
    }

    @Override
    public List<String> getRecognizableSigns() {
        final List<String> res = new ArrayList<>();
        res.add("@" + Provider.class.getSimpleName() + " on class");
        if (option(InstallersOptions.JerseyExtensionsRecognizedByType)) {
            for (Class<?> ext : EXTENSION_TYPES) {
                res.add("implements " + ext.getSimpleName());
            }
        }
        return res;
    }
}
