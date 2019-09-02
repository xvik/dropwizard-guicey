package ru.vyarus.dropwizard.guice.module.installer.feature.eager;


import com.google.common.base.Preconditions;
import com.google.inject.Binder;
import com.google.inject.Binding;
import com.google.inject.spi.DefaultBindingScopingVisitor;
import ru.vyarus.dropwizard.guice.module.installer.FeatureInstaller;
import ru.vyarus.dropwizard.guice.module.installer.install.binding.BindingInstaller;
import ru.vyarus.dropwizard.guice.module.installer.order.Order;
import ru.vyarus.dropwizard.guice.module.installer.util.FeatureUtils;
import ru.vyarus.dropwizard.guice.module.installer.util.Reporter;

import java.util.LinkedHashSet;
import java.util.Set;

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
@Order(50)
public class EagerSingletonInstaller implements FeatureInstaller<Object>, BindingInstaller {
    private static final ScopingVisitor VISITOR = new ScopingVisitor();

    private final Reporter reporter = new Reporter(EagerSingletonInstaller.class, "eager singletons =");
    private final Set<String> prerender = new LinkedHashSet<>();

    @Override
    public boolean matches(final Class<?> type) {
        return FeatureUtils.hasAnnotation(type, EagerSingleton.class);
    }

    @Override
    public void bindExtension(final Binder binder, final Class<?> type, final boolean lazy) {
        Preconditions.checkArgument(!lazy, "Eager bean can't be annotated as lazy: %s", type.getName());
        binder.bind(type).asEagerSingleton();
    }

    @Override
    public <T> void checkBinding(final Binder binder, final Class<T> type, final Binding<T> manualBinding) {
        // we can only validate existing binding here (actually entire extension is pretty useless in case of manual
        // binding)
        Preconditions.checkArgument(manualBinding.acceptScopingVisitor(VISITOR),
                "Eager bean, declared manually is not marked .asEagerSingleton(): %s", type.getName());
    }

    @Override
    public void installBinding(final Binder binder, final Class<?> type) {
        // may be called multiple times if bindings report enabled, but log must be counted just once
        prerender.add(String.format("(%s)", type.getName()));
    }

    @Override
    public void report() {
        for (String line : prerender) {
            reporter.line(line);
        }
        prerender.clear();
        reporter.report();
    }

    /**
     * Visitor detects eager singleton configuration on binding.
     */
    private static class ScopingVisitor extends DefaultBindingScopingVisitor<Boolean> {
        @Override
        protected Boolean visitOther() {
            return false;
        }

        @Override
        public Boolean visitEagerSingleton() {
            return true;
        }
    }
}
