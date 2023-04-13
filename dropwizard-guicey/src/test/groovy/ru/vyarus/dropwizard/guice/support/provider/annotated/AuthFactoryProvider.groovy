package ru.vyarus.dropwizard.guice.support.provider.annotated


import org.glassfish.jersey.server.ContainerRequest
import org.glassfish.jersey.server.internal.inject.AbstractValueParamProvider
import org.glassfish.jersey.server.internal.inject.MultivaluedParameterExtractorProvider
import org.glassfish.jersey.server.model.Parameter

import jakarta.inject.Inject
import jakarta.ws.rs.ext.Provider
import java.util.function.Function

/**
 * @author Vyacheslav Rusakov 
 * @since 20.11.2014
 */
@Provider
class AuthFactoryProvider extends AbstractValueParamProvider {

    Function<ContainerRequest, User> authFactory;

    @Inject
    public AuthFactoryProvider(final jakarta.inject.Provider<MultivaluedParameterExtractorProvider> extractorProvider,
                               // also Provider<AuthFactory> could be used
                               final AuthFactory factory) {
        super(extractorProvider, Parameter.Source.UNKNOWN);
        this.authFactory = factory;
    }

    @Override
    protected Function<ContainerRequest, User> createValueProvider(Parameter parameter) {
        final Auth auth = parameter.getAnnotation(Auth.class);
        return auth != null ? authFactory : null
    }
}
