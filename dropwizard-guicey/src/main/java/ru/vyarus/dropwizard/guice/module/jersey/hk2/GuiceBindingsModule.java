package ru.vyarus.dropwizard.guice.module.jersey.hk2;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.binder.ScopedBindingBuilder;
import com.google.inject.servlet.RequestScoped;
import org.glassfish.jersey.server.AsyncContext;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.internal.inject.MultivaluedParameterExtractorProvider;

import jakarta.inject.Provider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.*;
import jakarta.ws.rs.ext.Providers;

import static ru.vyarus.dropwizard.guice.module.installer.util.JerseyBinding.bindJerseyComponent;

/**
 * Registers important services from jersey context, making them available for injection in guice beans.
 * <ul>
 * <li>{@link jakarta.ws.rs.core.Application}
 * <li>{@link jakarta.ws.rs.ext.Providers}
 * <li>{@link jakarta.ws.rs.core.UriInfo}
 * <li>{@link jakarta.ws.rs.container.ResourceInfo}
 * <li>{@link jakarta.ws.rs.core.HttpHeaders}
 * <li>{@link jakarta.ws.rs.core.SecurityContext}
 * <li>{@link jakarta.ws.rs.core.Request}
 * <li>{@link org.glassfish.jersey.server.ContainerRequest}
 * <li>{@link org.glassfish.jersey.server.internal.inject.MultivaluedParameterExtractorProvider}
 * <li>{@link org.glassfish.jersey.server.AsyncContext}</li>
 * </ul>
 * <p>
 * When guice servlet modules support disabled, enables bindings for http request and response objects,
 * which will work only within resources (because HK2 doesn't monitor outer scopes).
 * <p>
 * {@link org.glassfish.jersey.internal.inject.InjectionManager} is registered by
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
