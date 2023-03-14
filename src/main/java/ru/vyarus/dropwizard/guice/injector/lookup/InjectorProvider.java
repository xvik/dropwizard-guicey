package ru.vyarus.dropwizard.guice.injector.lookup;

import com.google.inject.Injector;
import io.dropwizard.core.Application;

import javax.inject.Provider;

/**
 * Lazy injector provider. Used internally instead of direct injector reference when injector is not constructed yet.
 * It is not registered in guice context and used purely as lazy reference to injector during startup
 * (in jersey bindings logic).
 *
 * @author Vyacheslav Rusakov
 * @see InjectorLookup
 * @since 19.04.2015
 */
public class InjectorProvider implements Provider<Injector> {

    private final Application application;
    private Injector injector;

    public InjectorProvider(final Application application) {
        this.application = application;
    }

    @Override
    public Injector get() {
        if (injector == null) {
            injector = InjectorLookup.getInjector(application).get();
        }
        return injector;
    }
}
