package ru.vyarus.dropwizard.guice.support.provider.annotatedhkmanaged

import org.glassfish.jersey.server.internal.inject.ParamInjectionResolver
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.JerseyManaged
import ru.vyarus.dropwizard.guice.support.provider.annotated.Auth
import ru.vyarus.dropwizard.guice.support.provider.annotated.AuthFactoryProvider

import javax.ws.rs.ext.Provider

/**
 * @author Vyacheslav Rusakov 
 * @since 20.11.2014
 */
@Provider
@JerseyManaged
class AuthInjectionResolverHK extends ParamInjectionResolver<Auth> {
    AuthInjectionResolverHK() {
        super(AuthFactoryProvider)
    }
}
