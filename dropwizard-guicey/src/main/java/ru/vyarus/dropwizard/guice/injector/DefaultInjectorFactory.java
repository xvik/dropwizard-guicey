package ru.vyarus.dropwizard.guice.injector;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;

/**
 * Default implementation of {@link ru.vyarus.dropwizard.guice.injector.InjectorFactory} that simply delegates
 * to {@link com.google.inject.Guice#createInjector(com.google.inject.Stage, com.google.inject.Module...)}.
 *
 * @author Nicholas Pace
 * @since Dec 26, 2014
 */

public class DefaultInjectorFactory implements InjectorFactory {

    @Override
    public Injector createInjector(final Stage stage, final Iterable<? extends Module> modules) {
        return Guice.createInjector(stage, modules);
    }
}
