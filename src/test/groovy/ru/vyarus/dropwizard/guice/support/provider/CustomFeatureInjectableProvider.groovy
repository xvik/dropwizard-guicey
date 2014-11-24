package ru.vyarus.dropwizard.guice.support.provider

import org.glassfish.jersey.server.internal.inject.AbstractContainerRequestValueFactory
import ru.vyarus.dropwizard.guice.support.feature.CustomFeature

import javax.ws.rs.ext.Provider

/**
 * Sample injectable provider for CustomFeature type.
 *
 * @author Vyacheslav Rusakov 
 * @since 09.10.2014
 */
@Provider
class CustomFeatureInjectableProvider extends AbstractContainerRequestValueFactory<CustomFeature> {

    static int creationCounter = 0
    static int callCounter = 0

    CustomFeatureInjectableProvider() {
        creationCounter++
    }

    @Override
    CustomFeature provide() {
        callCounter++
        return new CustomFeature();
    }

    public static void resetCounters() {
        creationCounter = 0
        callCounter = 0
    }
}
