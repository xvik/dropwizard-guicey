package ru.vyarus.dropwizard.guice.support.provider.annotatedhkmanaged


import org.glassfish.jersey.server.ContainerRequest
import org.glassfish.jersey.server.internal.inject.AbstractValueParamProvider
import org.glassfish.jersey.server.internal.inject.MultivaluedParameterExtractorProvider
import org.glassfish.jersey.server.model.Parameter
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.HK2Managed
import ru.vyarus.dropwizard.guice.support.provider.annotated.Auth
import ru.vyarus.dropwizard.guice.support.provider.annotated.User

import javax.inject.Inject
import javax.ws.rs.ext.Provider
import java.util.function.Function

/**
 * @author Vyacheslav Rusakov 
 * @since 20.11.2014
 */
@Provider
@HK2Managed
// hk2 will create bean not guice!
class AuthFactoryProviderHK extends AbstractValueParamProvider {

    Function<ContainerRequest, User> authFactory;

    @Inject
    public AuthFactoryProviderHK(final javax.inject.Provider<MultivaluedParameterExtractorProvider> extractorProvider,
                                 // also Provider<AuthFactory> could be used
                                 final AuthFactoryHK factory) {
        super(extractorProvider, Parameter.Source.UNKNOWN);
        this.authFactory = factory;
    }

    @Override
    protected Function<ContainerRequest, ?> createValueProvider(Parameter parameter) {
        final Auth auth = parameter.getAnnotation(Auth.class);
        return auth != null ? authFactory : null
    }
}
