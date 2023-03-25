package ru.vyarus.guicey.annotations.lifecycle;

import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matcher;
import com.google.inject.matcher.Matchers;
import ru.vyarus.dropwizard.guice.module.context.unique.item.UniqueGuiceyBundle;
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyEnvironment;
import ru.vyarus.guice.ext.core.util.ObjectPackageMatcher;
import ru.vyarus.guicey.annotations.lifecycle.module.DropwizardLifecycleListener;
import ru.vyarus.guicey.annotations.lifecycle.module.LifecycleAnnotationsModule;

/**
 * Bundle enabled usage of lifecycle annotations in guice beans. Supported annotations:
 * <ul>
 * <li>{@link javax.annotation.PostConstruct} - same as {@link io.dropwizard.lifecycle.Managed#start()}</li>
 * <li>{@link PostStartup} - called after server startup
 * (dropwizard {@link io.dropwizard.lifecycle.ServerLifecycleListener} used)</li>
 * <li>{@link javax.annotation.PreDestroy} - same as {@link io.dropwizard.lifecycle.Managed#stop()}</li>
 * </ul>
 * <p>
 * The main intention is to replace usages of {@link io.dropwizard.lifecycle.Managed} beans with annotations, because
 * registration of managed beans requires classpath scan usage or manual extensions registration.
 * <p>
 * Annotations may be applied to any method without arguments. Annotations in super classes would also be detected.
 * One method could have multiple annotations. Multiple methods could be annotated with the same annotation.
 * <p>
 * If bean is registered after events processing (lazy bean, created by JIT) then event methods will be process
 * immediately after bean creation (with warnings in log).
 * <p>
 * Bundle is installed automatically using guicey bundles lookup. If you need to limit the scope of beans to search
 * annotations on then register bundle directly with declared custom matcher. For example:
 * <pre>{@code
 *      builder.bundles(new LifecycleAnnotationsBundle("package.to.apply"))
 * }</pre>
 * (only one instance of bundle will be used)
 *
 * @author Vyacheslav Rusakov
 * @since 08.11.2018
 */
public class LifecycleAnnotationsBundle extends UniqueGuiceyBundle {

    private final Matcher<? super TypeLiteral<?>> typeMatcher;

    /**
     * Default module constructor to check annotations on all beans.
     */
    public LifecycleAnnotationsBundle() {
        this(Matchers.any());
    }

    /**
     * Constructs annotation module with annotation scan limited to provided package.
     * (used mainly for startup performance optimization)
     *
     * @param pkg package to limit beans, where annotations processed
     */
    public LifecycleAnnotationsBundle(final String pkg) {
        this(new ObjectPackageMatcher<>(pkg));
    }

    /**
     * Constructs annotation module with custom bean matcher for annotations processing.
     *
     * @param typeMatcher matcher to select beans for annotations processing
     * @see ObjectPackageMatcher as example matcher implementation
     */
    public LifecycleAnnotationsBundle(final Matcher<? super TypeLiteral<?>> typeMatcher) {
        this.typeMatcher = typeMatcher;
    }

    @Override
    public void run(final GuiceyEnvironment environment) {
        final LifecycleAnnotationsModule module = new LifecycleAnnotationsModule(typeMatcher);
        final DropwizardLifecycleListener lifecycle = new DropwizardLifecycleListener(module.getCollector());

        environment
                .modules(module)
                // do not register as extension to not put additional beans into the guice context
                .manage(lifecycle)
                .listenServer(lifecycle);
    }
}
