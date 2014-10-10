package ru.vyarus.dropwizard.guice.module.installer.feature.eager;


import io.dropwizard.setup.Environment;
import ru.vyarus.dropwizard.guice.module.installer.FeatureInstaller;
import ru.vyarus.dropwizard.guice.module.installer.install.InstanceInstaller;
import ru.vyarus.dropwizard.guice.module.installer.util.FeatureUtils;

/**
 * Search for classes with {@code @Eager} annotation and register them in guice context.
 * This may be useful for outstanding classes (not injected by other beans and so not registered with guice.
 * Normally such classes must be manually registered, but using {@code @Eager} annotation allows to register them
 * automatically. Moreover, even in DEVELOPMENT stage instance will be requested, which makes class suitable
 * for initialization logic.
 */
public class EagerInstaller implements FeatureInstaller<Object>, InstanceInstaller<Object> {
    @Override
    public boolean matches(final Class<?> type) {
        return FeatureUtils.hasAnnotation(type, Eager.class);
    }

    @Override
    public void install(final Environment environment, final Object instance) {
        // instance installer required only to force bean instance creation (and so jsr250 annotations could act)
    }
}
