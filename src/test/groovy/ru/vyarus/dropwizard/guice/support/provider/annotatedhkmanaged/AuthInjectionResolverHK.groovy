package ru.vyarus.dropwizard.guice.support.provider.annotatedhkmanaged

import org.glassfish.jersey.server.internal.inject.ParamInjectionResolver
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.HK2Managed
import ru.vyarus.dropwizard.guice.module.installer.install.binding.LazyBinding
import ru.vyarus.dropwizard.guice.support.provider.annotated.Auth
import ru.vyarus.dropwizard.guice.support.provider.annotated.AuthFactoryProvider

import javax.ws.rs.ext.Provider

/**
 * @author Vyacheslav Rusakov 
 * @since 20.11.2014
 */
@Provider
@HK2Managed
class AuthInjectionResolverHK extends ParamInjectionResolver<Auth> {
    AuthInjectionResolverHK() {
        super(AuthFactoryProvider)
    }
}
