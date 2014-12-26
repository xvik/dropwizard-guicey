package ru.vyarus.dropwizard.guice.api;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;

/**
 * This interface may be implemented to provide custom Guice injectors and support certain types of Guice extension
 * libraries.
 *
 * @author Nicholas Pace
 * @since Dec 26, 2014
 */
public interface InjectorFactory {

    /**
     * Creates an injector instance.
     * 
     * @param stage
     *            The stage for which the injector is created.
     * @param modules
     *            A list of modules for the injector.
     * @return A new injector.
     */
    Injector createInjector(Stage stage, Iterable<? extends Module> modules);
}
