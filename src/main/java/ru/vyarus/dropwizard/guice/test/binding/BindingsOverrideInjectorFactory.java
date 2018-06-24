package ru.vyarus.dropwizard.guice.test.binding;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;
import com.google.inject.util.Modules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vyarus.dropwizard.guice.injector.InjectorFactory;
import ru.vyarus.dropwizard.guice.module.context.debug.util.RenderUtils;

/**
 * Custom injector factory used to override already overridden bindings.
 * Note that in most cases direct override support in
 * {@link ru.vyarus.dropwizard.guice.GuiceBundle.Builder#modulesOverride(Module...)} will be enough.
 * Custom injector factory may be required to override binding, already overridden by modules override feature.
 * <p>
 * Usage:
 * <ul>
 * <li>Register custom factory in main bundle: {@code bundle.injectorFactory(new BindingsOverrideInjectorFactory())}
 * </li>
 * <li>Register modules with overriding bindings using {@link #override(Module...)}.</li>
 * </ul>
 * <p>
 * Note that factory may be registered using {@link ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook}
 * (in spock or junit extension). Custom modules could be registered with hook too because hook provides a good
 * integration point before application start.
 * <p>
 * It is assumed that modules will be registered at the same thread where application will initialize
 * (thread local state used to support parallel testing).
 *
 * @author Vyacheslav Rusakov
 * @since 24.06.2018
 */
public class BindingsOverrideInjectorFactory implements InjectorFactory {

    private static final ThreadLocal<Module[]> OVERRIDING_MODULES = new ThreadLocal<>();
    // used to catch too late moduels registration at the current thread
    private static final ThreadLocal<Boolean> TOO_LATE = new ThreadLocal<>();

    private final Logger logger = LoggerFactory.getLogger(BindingsOverrideInjectorFactory.class);

    public BindingsOverrideInjectorFactory() {
        // assumed that factory registered as instance before test run and so thread will always use clear state
        OVERRIDING_MODULES.remove();
        TOO_LATE.remove();
    }

    @Override
    public Injector createInjector(final Stage stage, final Iterable<? extends Module> modules) {
        final Module[] override = OVERRIDING_MODULES.get();
        OVERRIDING_MODULES.remove();
        TOO_LATE.set(true);
        if (override != null) {
            printOverridingModules(override);
        }
        return Guice.createInjector(stage, override == null ? modules
                : Lists.newArrayList(Modules.override(modules).with(override)));
    }

    /**
     * Overriding bindings registration. Note that modules should contain only overriding bindings - all other
     * will be used from main modules.
     *
     * @param modules modules containing overriding bindings
     * @throws IllegalStateException if injector was already created to prevent bad usage
     */
    public static void override(final Module... modules) {
        Preconditions.checkState(TOO_LATE.get() == null,
                "Too late overriding bindings registration: injector was already created");
        OVERRIDING_MODULES.set(modules);
    }


    private void printOverridingModules(final Module... modules) {
        final StringBuilder builder = new StringBuilder().append("\n\n");
        for (Module module : modules) {
            builder.append('\t').append(RenderUtils
                    .renderClassLine(module.getClass(), null))
                    .append('\n');
        }
        logger.info("Overriding modules = {}", builder.toString());
    }
}
