package ru.vyarus.dropwizard.guice.support.provider

import ru.vyarus.dropwizard.guice.support.feature.CustomFeature

import jakarta.ws.rs.ext.Provider
import java.util.function.Supplier

/**
 * Sample injectable provider for CustomFeature type.
 *
 * @author Vyacheslav Rusakov 
 * @since 09.10.2014
 */
@Provider
class CustomFeatureInjectableProvider implements Supplier<CustomFeature> {

    static int creationCounter = 0
    static int callCounter = 0

    CustomFeatureInjectableProvider() {
        creationCounter++
    }

    @Override
    CustomFeature get() {
        callCounter++
        return new CustomFeature();
    }

    public static void resetCounters() {
        creationCounter = 0
        callCounter = 0
    }
}
