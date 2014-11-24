package ru.vyarus.dropwizard.guice.support.provider.annotated

import org.glassfish.hk2.api.Factory
import org.glassfish.hk2.api.ServiceLocator
import org.glassfish.jersey.server.internal.inject.AbstractValueFactoryProvider
import org.glassfish.jersey.server.internal.inject.MultivaluedParameterExtractorProvider
import org.glassfish.jersey.server.model.Parameter
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.HK2Managed
import ru.vyarus.dropwizard.guice.module.installer.install.binding.LazyBinding

import javax.inject.Inject
import javax.ws.rs.ext.Provider

/**
 * @author Vyacheslav Rusakov 
 * @since 20.11.2014
 */
@Provider
@LazyBinding // @HK2Managed may be used as alternative
class AuthFactoryProvider extends AbstractValueFactoryProvider {

    Factory<User> authFactory;

    @Inject
    public AuthFactoryProvider(final MultivaluedParameterExtractorProvider extractorProvider,
                               final Factory<User> factory, // also Provider<AuthFactory> could be used
                               final ServiceLocator injector) {
        super(extractorProvider, injector, Parameter.Source.UNKNOWN);
        this.authFactory = factory;
    }

    @Override
    protected Factory<?> createValueFactory(Parameter parameter) {
        final Auth auth = parameter.getAnnotation(Auth.class);
        return auth != null ? authFactory : null
    }
}
