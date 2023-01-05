package ru.vyarus.dropwizard.guice.injector.jersey;

import com.google.inject.Injector;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.inject.InjectionManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vyarus.dropwizard.guice.GuiceBundle;

/**
 * @author Vyacheslav Rusakov
 * @since 23.04.2019
 */
public class GuiceyInjectionFactory implements InjectionManagerFactory {
    private static final ThreadLocal<Injector> holder = new ThreadLocal<>();
    private final Logger logger = LoggerFactory.getLogger(GuiceyInjectionFactory.class);

    public static void businessContextStarted(Injector injector) {
        holder.set(injector);
    }

    /**
     * It must be impossible to reach state when configuration from previous test run used, but, just in case,
     * this method allows to guarantee clear state.
     */
    public static void clear() {
        holder.remove();
    }

    @Override
    public InjectionManager create(final Object parent) {
        final Injector context = holder.get();
        clear();
        if (parent != null) {
            throw new IllegalStateException("Sub context creation not supported");
        }
        if (context == null) {
            throw new IllegalStateException("GUICE support NOT INITIALIZED: no " + GuiceBundle.class.getSimpleName()
                    + " registered by application.");
        }
        logger.debug("CREATE injection manager");
        return new GuiceyInjectionManager(context);
    }

    public static void noHK2() {
        try {
            Class.forName("org.glassfish.jersey.inject.hk2.Hk2InjectionManagerFactory");
            throw new IllegalStateException("Jersey-HK2 integration detected in classpath: remove");
        } catch (ClassNotFoundException e) {
            // OK
        }
    }
}
