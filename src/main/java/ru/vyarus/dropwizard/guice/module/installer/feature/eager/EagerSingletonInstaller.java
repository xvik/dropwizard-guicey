package ru.vyarus.dropwizard.guice.module.installer.feature.eager;


import com.google.common.base.Preconditions;
import com.google.inject.Binder;
import ru.vyarus.dropwizard.guice.module.installer.FeatureInstaller;
import ru.vyarus.dropwizard.guice.module.installer.install.binding.BindingInstaller;
import ru.vyarus.dropwizard.guice.module.installer.util.FeatureUtils;
import ru.vyarus.dropwizard.guice.module.installer.util.Reporter;

/**
 * Search for classes with {@code @EagerSingleton} annotation and register them in guice context.
 * This may be useful for outstanding classes (not injected by other beans and so not registered with guice.
 * Normally such classes must be manually registered, but using {@code @EagerSingleton} annotation allows
 * to register them automatically.
 * Moreover, even in DEVELOPMENT stage instance will be requested, which makes class suitable
 * for initialization logic.
 *
 * @author Vyacheslav Rusakov
 * @since 01.09.2014
 */
public class EagerSingletonInstaller implements FeatureInstaller<Object>, BindingInstaller {
    private final Reporter reporter = new Reporter(EagerSingletonInstaller.class, "eager singletons =");

    @Override
    public boolean matches(final Class<?> type) {
        return FeatureUtils.hasAnnotation(type, EagerSingleton.class);
    }

    @Override
    public <T> void install(final Binder binder, final Class<? extends T> type, final boolean lazy) {
        Preconditions.checkArgument(!lazy, "Eager bean can't be annotated as lazy: %s", type.getName());
        binder.bind(type).asEagerSingleton();
        reporter.line("(%s)", type.getName());
    }

    @Override
    public void report() {
        reporter.report();
    }
}
