package ru.vyarus.dropwizard.guice.cases.hkscope.support

import org.glassfish.jersey.internal.inject.Injectee
import org.glassfish.jersey.internal.inject.InjectionResolver
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.GuiceManaged

import javax.ws.rs.ext.Provider

/**
 * @author Vyacheslav Rusakov
 * @since 19.01.2016
 */
@Provider
@GuiceManaged
class GuiceInjectionResolver implements InjectionResolver<Ann> {

    @Override
    Object resolve(Injectee injectee) {
        return null
    }

    @Override
    Class<Ann> getAnnotation() {
        return Ann.class
    }

    @Override
    boolean isConstructorParameterIndicator() {
        return false
    }

    @Override
    boolean isMethodParameterIndicator() {
        return false
    }
}
