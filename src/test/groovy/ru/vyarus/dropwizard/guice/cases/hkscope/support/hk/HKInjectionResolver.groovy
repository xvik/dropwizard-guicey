package ru.vyarus.dropwizard.guice.cases.hkscope.support.hk

import org.glassfish.jersey.internal.inject.Injectee
import org.glassfish.jersey.internal.inject.InjectionResolver
import ru.vyarus.dropwizard.guice.cases.hkscope.support.Ann
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.HK2Managed

import javax.ws.rs.ext.Provider

/**
 * @author Vyacheslav Rusakov
 * @since 19.01.2016
 */
@Provider
@HK2Managed
class HKInjectionResolver implements InjectionResolver<Ann> {

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
