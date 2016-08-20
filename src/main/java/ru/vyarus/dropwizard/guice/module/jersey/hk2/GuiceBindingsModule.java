package ru.vyarus.dropwizard.guice.module.jersey.hk2;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.binder.ScopedBindingBuilder;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.internal.inject.MultivaluedParameterExtractorProvider;
import org.glassfish.jersey.server.internal.process.AsyncContext;

import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.*;
import javax.ws.rs.ext.Providers;

import static ru.vyarus.dropwizard.guice.module.installer.util.JerseyBinding.bindJerseyComponent;

/**
 * Registers important services from HK2 context, making them available for injection in guice beans.
 * <ul>
 * <li>{@link javax.ws.rs.core.Application}
 * <li>{@link javax.ws.rs.ext.Providers}
 * <li>{@link javax.ws.rs.core.UriInfo}
 * <li>{@link javax.ws.rs.core.HttpHeaders}
 * <li>{@link javax.ws.rs.core.SecurityContext}
 * <li>{@link javax.ws.rs.core.Request}
 * <li>{@link org.glassfish.jersey.server.ContainerRequest}
 * <li>{@link org.glassfish.jersey.server.internal.inject.MultivaluedParameterExtractorProvider}
 * <li>{@link org.glassfish.jersey.server.internal.process.AsyncContext}</li>
 * </ul>
 * <p>
 * When guice servlet modules support disabled, enables bindings for http request and response objects,
 * which will work only within resources (because hk doesn't monitor outer scopes).
 * <p>
 * {@link org.glassfish.hk2.api.ServiceLocator} is registered by
 * {@link ru.vyarus.dropwizard.guice.module.jersey.GuiceFeature}
 *
 * @author Vyacheslav Rusakov
 * @since 15.11.2014
 */
public class GuiceBindingsModule extends AbstractModule {

    private final Provider<Injector> provider;
    private final boolean guiceServletSupport;

    public GuiceBindingsModule(final Provider<Injector> provider, final boolean guiceServletSupport) {
        this.provider = provider;
        this.guiceServletSupport = guiceServletSupport;
    }

    @Override
    protected void configure() {
        jerseyToGuice(MultivaluedParameterExtractorProvider.class);
        jerseyToGuice(Application.class);
        jerseyToGuice(Providers.class);

        // request scoped objects, but hk will control their scope
        // must be used in guice only with Provider
        jerseyToGuice(UriInfo.class);
        jerseyToGuice(HttpHeaders.class);
        jerseyToGuice(SecurityContext.class);
        jerseyToGuice(Request.class);
        jerseyToGuice(ContainerRequest.class);
        jerseyToGuice(AsyncContext.class);

        if (!guiceServletSupport) {
            // bind request and response objects when guice servlet module not registered
            // but this will work only for resources
            jerseyToGuice(HttpServletRequest.class);
            jerseyToGuice(HttpServletResponse.class);
        }
    }

    private ScopedBindingBuilder jerseyToGuice(final Class<?> type) {
        return bindJerseyComponent(binder(), provider, type);
    }
}
