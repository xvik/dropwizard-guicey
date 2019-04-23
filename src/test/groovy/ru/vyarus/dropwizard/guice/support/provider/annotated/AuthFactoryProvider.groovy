package ru.vyarus.dropwizard.guice.support.provider.annotated

import org.glassfish.hk2.api.Factory
import org.glassfish.jersey.server.ContainerRequest
import org.glassfish.jersey.server.internal.inject.AbstractValueParamProvider
import org.glassfish.jersey.server.internal.inject.MultivaluedParameterExtractorProvider
import org.glassfish.jersey.server.model.Parameter
import ru.vyarus.dropwizard.guice.module.installer.install.binding.LazyBinding

import javax.inject.Inject
import javax.ws.rs.ext.Provider
import java.util.function.Function

/**
 * @author Vyacheslav Rusakov 
 * @since 20.11.2014
 */
@Provider
@LazyBinding
// @HK2Managed may be used as alternative
class AuthFactoryProvider extends AbstractValueParamProvider {

    Function<ContainerRequest, User> authFactory;

    @Inject
    public AuthFactoryProvider(final javax.inject.Provider<MultivaluedParameterExtractorProvider> extractorProvider,
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
