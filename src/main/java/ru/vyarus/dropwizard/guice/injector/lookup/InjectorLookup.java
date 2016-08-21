package ru.vyarus.dropwizard.guice.injector.lookup;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.inject.Injector;
import io.dropwizard.Application;
import io.dropwizard.lifecycle.Managed;

import java.util.Map;
import java.util.Optional;

/**
 * Injector lookup utility. Injectors registered automatically (by GuiceBundle).
 *
 * @author Vyacheslav Rusakov
 * @since 19.04.2015
 */
public final class InjectorLookup {

    private static final Map<Application, Injector> INJECTORS = Maps.newConcurrentMap();

    private InjectorLookup() {
    }

    /**
     * Used internally to register application specific injector.
     *
     * @param application application instance
     * @param injector    injector instance
     * @return managed object, which must be registered to remove injector on application stop
     */
    public static Managed registerInjector(final Application application, final Injector injector) {
        Preconditions.checkNotNull(application, "Application instance required");
        Preconditions.checkArgument(!INJECTORS.containsKey(application),
                "Injector already registered for application %s", application.getClass().getName());
        INJECTORS.put(application, injector);
        return new Managed() {
            @Override
            public void start() throws Exception {
                // not used
            }

            @Override
            public void stop() throws Exception {
                INJECTORS.remove(application);
            }
        };
    }

    /**
     * @param application application instance
     * @return optional with or without application-bound injector
     */
    public static Optional<Injector> getInjector(final Application application) {
        return Optional.ofNullable(INJECTORS.get(application));
    }

    /**
     * Clears stored injectors references. Normally shouldn't be used at all, because managed object, returned
     * on registration removes injector reference automatically on shutdown (for example,
     * when used DropwizardAppRule or GuiceyAppRule).
     * May be useful in tests, when application was not shut down properly.
     * <p>WARNING: calling this method while application is working may cause incorrect behaviour.</p>
     */
    public static void clear() {
        INJECTORS.clear();
    }
}
