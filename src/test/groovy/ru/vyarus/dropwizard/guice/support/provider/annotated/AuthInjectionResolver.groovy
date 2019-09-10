package ru.vyarus.dropwizard.guice.support.provider.annotated

import org.glassfish.jersey.server.internal.inject.ParamInjectionResolver
import ru.vyarus.dropwizard.guice.module.installer.install.binding.LazyBinding

import javax.ws.rs.ext.Provider

/**
 * @author Vyacheslav Rusakov 
 * @since 20.11.2014
 */
//@JerseyManaged // there are two ways to solve this bean creation: use lazy init (bean will not be created by guice context
// and will be created after hk2 binding, so service locator injection will work; another way is to delegate bean creation
// to hk2
@Provider
@LazyBinding
class AuthInjectionResolver extends ParamInjectionResolver<Auth> {
    AuthInjectionResolver() {
        super(AuthFactoryProvider)
    }
}
