package ru.vyarus.dropwizard.guice.module.installer.feature.eager;


import com.google.common.base.Preconditions;
import com.google.inject.Binder;
import com.google.inject.Binding;
import com.google.inject.Stage;
import ru.vyarus.dropwizard.guice.debug.report.guice.util.visitor.GuiceScopingVisitor;
import ru.vyarus.dropwizard.guice.debug.util.RenderUtils;
import ru.vyarus.dropwizard.guice.module.installer.FeatureInstaller;
import ru.vyarus.dropwizard.guice.module.installer.install.binding.BindingInstaller;
import ru.vyarus.dropwizard.guice.module.installer.order.Order;
import ru.vyarus.dropwizard.guice.module.installer.util.BindingUtils;
import ru.vyarus.dropwizard.guice.module.installer.util.FeatureUtils;
import ru.vyarus.dropwizard.guice.module.installer.util.Reporter;

import javax.inject.Singleton;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
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
public class EagerSingletonInstaller implements FeatureInstaller, BindingInstaller {
    private static final GuiceScopingVisitor VISITOR = new GuiceScopingVisitor();

    private final Reporter reporter = new Reporter(EagerSingletonInstaller.class, "eager singletons =");
    private final Set<String> prerender = new LinkedHashSet<>();

    @Override
    public boolean matches(final Class<?> type) {
        return FeatureUtils.hasAnnotation(type, EagerSingleton.class);
    }

    @Override
    public void bind(final Binder binder, final Class<?> type, final boolean lazy) {
        Preconditions.checkArgument(!lazy, "Eager bean can't be annotated as lazy: %s", type.getName());
        binder.bind(type).asEagerSingleton();
    }

    @Override
    public <T> void manualBinding(final Binder binder, final Class<T> type, final Binding<T> binding) {
        // we can only validate existing binding here (actually entire extension is pretty useless in case of manual
        // binding)
        final Class<? extends Annotation> scope = VISITOR.performDetection(binding);
        // in production, all services will work as eager singletons, for report (TOOL stage) consider also valid
        Preconditions.checkArgument(scope.equals(EagerSingleton.class)
                        || (!binder.currentStage().equals(Stage.DEVELOPMENT)
                        && (scope.equals(Singleton.class) || scope.equals(jakarta.inject.Singleton.class))),
                // intentionally no "at" before stacktrtace because idea may hide error in some cases
                "Eager bean, declared manually is not marked .asEagerSingleton(): %s (%s)",
                type.getName(), BindingUtils.getDeclarationSource(binding));
    }

    @Override
    public void extensionBound(final Stage stage, final Class<?> type) {
        if (stage != Stage.TOOL) {
            // may be called multiple times if bindings report enabled, but log must be counted just once
            prerender.add(String.format("%s", RenderUtils.renderClassLine(type)));
        }
    }

    @Override
    public void report() {
        for (String line : prerender) {
            reporter.line(line);
        }
        prerender.clear();
        reporter.report();
    }

    @Override
    public List<String> getRecognizableSigns() {
        return Collections.singletonList("@" + EagerSingleton.class.getSimpleName() + " on class");
    }
}
