package ru.vyarus.dropwizard.guice.module.installer.feature.eager;


import com.google.inject.Binder;
import ru.vyarus.dropwizard.guice.module.installer.FeatureInstaller;
import ru.vyarus.dropwizard.guice.module.installer.install.BindingInstaller;
import ru.vyarus.dropwizard.guice.module.installer.util.FeatureUtils;

/**
 * Search for classes with {@code @Eager} annotation and register them in guice context.
 * This may be useful for outstanding classes (not injected by other beans and so not registered with guice.
 * Normally such classes must be manually registered, but using {@code @Eager} annotation allows to register them
 * automatically. Moreover, even in DEVELOPMENT stage instance will be requested, which makes class suitable
 * for initialization logic.
 */
public class EagerSingletonInstaller implements FeatureInstaller<Object>, BindingInstaller {
    @Override
    public boolean matches(final Class<?> type) {
        return FeatureUtils.hasAnnotation(type, EagerSingleton.class);
    }

    @Override
    public <T> void install(final Binder binder, final Class<? extends T> type) {
        binder.bind(type).asEagerSingleton();
    }
}
