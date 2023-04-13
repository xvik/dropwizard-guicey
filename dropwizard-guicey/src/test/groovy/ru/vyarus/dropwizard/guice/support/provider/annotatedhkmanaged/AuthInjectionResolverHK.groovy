package ru.vyarus.dropwizard.guice.support.provider.annotatedhkmanaged

import org.glassfish.jersey.server.ContainerRequest
import org.glassfish.jersey.server.internal.inject.ParamInjectionResolver
import org.glassfish.jersey.server.model.Parameter
import org.glassfish.jersey.server.spi.internal.ValueParamProvider
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.JerseyManaged
import ru.vyarus.dropwizard.guice.support.provider.annotated.Auth
import ru.vyarus.dropwizard.guice.support.provider.annotated.AuthFactory
import ru.vyarus.dropwizard.guice.support.provider.annotated.AuthFactoryProvider

import jakarta.inject.Inject
import jakarta.ws.rs.ext.Provider
import java.util.function.Function

/**
 * @author Vyacheslav Rusakov 
 * @since 20.11.2014
 */
@Provider
@JerseyManaged
class AuthInjectionResolverHK extends ParamInjectionResolver<Auth> {
    @Inject
    AuthInjectionResolverHK(jakarta.inject.Provider<ContainerRequest> request, AuthFactory factory) {
        super(new ParamProvider(factory), AuthFactoryProvider, request)
    }


    static class ParamProvider implements ValueParamProvider {
        AuthFactory factory;

        ParamProvider(AuthFactory factory) {
            this.factory = factory
        }

        @Override
        Function<ContainerRequest, ?> getValueProvider(Parameter parameter) {
            return factory
        }

        @Override
        PriorityType getPriority() {
            return Priority.LOW
        }
    }
}
