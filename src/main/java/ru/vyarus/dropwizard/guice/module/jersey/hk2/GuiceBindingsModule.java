package ru.vyarus.dropwizard.guice.module.jersey.hk2;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.binder.ScopedBindingBuilder;
import com.google.inject.servlet.RequestScoped;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.internal.inject.MultivaluedParameterExtractorProvider;
import org.glassfish.jersey.server.internal.process.AsyncContext;

import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.*;
import javax.ws.rs.ext.Providers;

import static ru.vyarus.dropwizard.guice.module.installer.util.JerseyBinding.bindJerseyComponent;

/**
 * Registers important services from HK2 context, making them available for injection in guice beans.
 * <ul>
 * <li>{@link javax.ws.rs.core.Application}
 * <li>{@link javax.ws.rs.ext.Providers}
 * <li>{@link javax.ws.rs.core.UriInfo}
 * <li>{@link javax.ws.rs.container.ResourceInfo}
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
        jerseyToGuiceGlobal(MultivaluedParameterExtractorProvider.class);
        jerseyToGuiceGlobal(Application.class);
        jerseyToGuiceGlobal(Providers.class);

        // request scoped objects
        jerseyToGuice(UriInfo.class);
        jerseyToGuice(ResourceInfo.class);
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

    private void jerseyToGuiceGlobal(final Class<?> type) {
        jerseyToGuiceBinding(type, true);
    }

    private void jerseyToGuice(final Class<?> type) {
        jerseyToGuiceBinding(type, false);
    }

    /**
     * Important moment: request scoped jersey objects must be bound to guice request scope (if guice web used)
     * because otherwise scope delegation to other thread will not work
     * (see {@link com.google.inject.servlet.ServletScopes#transferRequest(java.util.concurrent.Callable)}).
     * <p>
     * WARNING: bean instance must be obtained in current (request) thread in order to be us used later
     * inside transferred thread (simply call {@code provider.get()} (for jersey-managed bean like {@link UriInfo})
     * before {@code ServletScopes.transferRequest()}.
     *
     * @param type   jersey type to bind
     * @param global true for global type binding
     */
    private void jerseyToGuiceBinding(final Class<?> type, final boolean global) {
        final ScopedBindingBuilder binding = bindJerseyComponent(binder(), provider, type);
        if (!global && guiceServletSupport) {
            binding.in(RequestScoped.class);
        }
    }
}
