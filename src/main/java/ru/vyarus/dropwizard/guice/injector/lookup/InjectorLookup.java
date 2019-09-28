package ru.vyarus.dropwizard.guice.injector.lookup;

import com.google.inject.Injector;
import io.dropwizard.Application;
import ru.vyarus.dropwizard.guice.module.context.SharedConfigurationState;

import java.util.Optional;

/**
 * Application injector static lookup utility.
 *
 * @author Vyacheslav Rusakov
 * @since 19.04.2015
 */
public final class InjectorLookup {

    private InjectorLookup() {
    }

    /**
     * @param application application instance
     * @return optional with or without application-bound injector
     */
    public static Optional<Injector> getInjector(final Application application) {
        return SharedConfigurationState.lookup(application, Injector.class);
    }

    /**
     * Used internally to register application specific injector.
     *
     * @param application application instance
     * @param injector    injector instance
     */
    public static void registerInjector(final Application application, final Injector injector) {
        // This method is actually not required anymore as guicey can register state directly
        // Preserving for backwards compatibility (possible usages in test integrations)
        SharedConfigurationState.getOrFail(application, "No shared state assigned to application")
                .put(Injector.class, injector);
    }
}
