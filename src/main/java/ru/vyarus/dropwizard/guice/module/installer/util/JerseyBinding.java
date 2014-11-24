package ru.vyarus.dropwizard.guice.module.installer.util;

import com.google.inject.Binder;
import com.google.inject.binder.ScopedBindingBuilder;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.InjectionResolver;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.hk2.utilities.reflection.ParameterizedTypeImpl;
import org.glassfish.jersey.server.spi.internal.ValueFactoryProvider;
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.HK2Managed;
import ru.vyarus.dropwizard.guice.module.jersey.support.GuiceComponentFactory;
import ru.vyarus.dropwizard.guice.module.jersey.support.JerseyComponentProvider;
import ru.vyarus.dropwizard.guice.module.jersey.support.LazyGuiceFactory;
import ru.vyarus.java.generics.resolver.GenericsResolver;

import javax.inject.Singleton;
import java.lang.annotation.Annotation;

/**
 * HK binding utilities. Supplement {@link ru.vyarus.dropwizard.guice.module.installer.install.JerseyInstaller}.
 *
 * @author Vyacheslav Rusakov
 * @since 21.11.2014
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class JerseyBinding {

    private JerseyBinding() {
    }

    /**
     * @param type type to check
     * @return true if type annotated with {@code HK2Managed}, false otherwise
     * @see ru.vyarus.dropwizard.guice.module.installer.feature.jersey.HK2Managed
     */
    public static boolean isHK2Managed(final Class<?> type) {
        return type.isAnnotationPresent(HK2Managed.class);
    }

    /**
     * Binds component into HK context. If component is annotated with {@code HK2Managed}, then registers type,
     * otherwise register guice "bridge" factory around component.
     *
     * @param binder hk binder
     * @param type   component type
     * @see ru.vyarus.dropwizard.guice.module.jersey.support.GuiceComponentFactory
     */
    @SuppressWarnings("unchecked")
    public static void bindComponent(final AbstractBinder binder, final Class<?> type) {
        if (isHK2Managed(type)) {
            binder.bind(type)
                    .in(Singleton.class);
        } else {
            // default case: simple service registered directly (including resource)
            binder.bindFactory(new GuiceComponentFactory(type))
                    .to(type);
        }
    }

    /**
     * Binds hk {@code Factory}. If bean is {@code @HK2Managed} then registered directly as
     * factory. Otherwise register factory through special "lazy bridge" to delay guice factory bean instantiation.
     * Also registers factory directly (through wrapper to be able to inject factory by its type).
     *
     * @param binder hk binder
     * @param type   factory to bind
     * @param <T>    actual type (used to workaround type checks)
     * @see ru.vyarus.dropwizard.guice.module.jersey.support.LazyGuiceFactory
     * @see ru.vyarus.dropwizard.guice.module.jersey.support.GuiceComponentFactory
     */
    @SuppressWarnings("unchecked")
    public static <T> void bindFactory(final AbstractBinder binder, final Class<?> type) {
        // resolve Factory<T> actual type to bind properly
        final Class<T> res = (Class<T>) GenericsResolver.resolve(type).type(Factory.class).generic(0);
        if (isHK2Managed(type)) {
            binder.bindFactory((Class<Factory<T>>) type)
                    .to(res)
                    .in(Singleton.class);
        } else {
            binder.bindFactory(new LazyGuiceFactory(type))
                    .to(res);
            // binding factory type to be able to autowire factory by name
            binder.bindFactory(new GuiceComponentFactory(type))
                    .to(type);
        }
    }

    /**
     * Binds {@code ValueFactoryProvider}. If type is {@code HK2Managed}, binds directly. Otherwise,
     * use guice "bridge" factory to lazily bind type.
     * Note: value factory provider are instantiated eagerly in hk context.
     *
     * @param binder hk binder
     * @param type   value factory provider type
     */
    @SuppressWarnings("unchecked")
    public static void bindValueFactoryProvider(final AbstractBinder binder, final Class<?> type) {
        if (isHK2Managed(type)) {
            binder.bind(type)
                    .to(ValueFactoryProvider.class)
                    .in(Singleton.class);
        } else {
            binder.bindFactory(new GuiceComponentFactory(type))
                    .to(type)
                    .in(Singleton.class);
            binder.bind(type)
                    .to(ValueFactoryProvider.class)
                    .in(Singleton.class);
        }
    }

    @SuppressWarnings("unchecked")
    public static void bindInjectionResolver(final AbstractBinder binder, final Class<?> type) {
        // resolve InjectionProvider<T> generic - it's actual binding annotation
        final Class<? extends Annotation> annotation = (Class<? extends Annotation>) GenericsResolver.resolve(type)
                .type(InjectionResolver.class).generic(0);
        // trick - bind to type and hk2 should use factory to obtain actual instance
        if (isHK2Managed(type)) {
            binder.bind(type)
                    .to(new ParameterizedTypeImpl(InjectionResolver.class, annotation))
                    .in(Singleton.class);
        } else {
            binder.bindFactory(new GuiceComponentFactory(type))
                    .to(type)
                    .in(Singleton.class);
            binder.bind(type)
                    .to(new ParameterizedTypeImpl(InjectionResolver.class, annotation))
                    .in(Singleton.class);
        }
    }

    /**
     * Used to bind jersey beans in guice context (lazily). Guice context is started first, so there is
     * no way to bind instances. Instead "lazy bridge" installed, which will resolve target type on first call.
     *
     * @param binder guice binder
     * @param type   jersey type to register
     * @param <T>    type
     * @return scoped binder object to optionally define binding scope.
     */
    @SuppressWarnings("unchecked")
    public static <T> ScopedBindingBuilder bindJerseyComponent(final Binder binder, final Class<T> type) {
        return binder.bind(type).toProvider(new JerseyComponentProvider<>(type));
    }

}
