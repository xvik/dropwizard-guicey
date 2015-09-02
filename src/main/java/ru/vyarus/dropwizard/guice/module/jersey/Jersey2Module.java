package ru.vyarus.dropwizard.guice.module.jersey;


import com.google.inject.servlet.GuiceFilter;
import com.google.inject.servlet.ServletModule;
import io.dropwizard.Application;
import io.dropwizard.setup.Environment;
import org.glassfish.hk2.api.ServiceLocator;
import ru.vyarus.dropwizard.guice.injector.lookup.InjectorProvider;
import ru.vyarus.dropwizard.guice.module.installer.internal.AdminGuiceFilter;
import ru.vyarus.dropwizard.guice.module.jersey.hk2.GuiceBindingsModule;

/**
 * Guice jersey2 integration module.
 * <p>Integration is very similar to old jersey-guice: guice context is dominant;
 * guice instantiated first; jersey objects directly registered in guice; guice objects directly registered
 * in hk.</p>
 *
 * @author Vyacheslav Rusakov
 * @see ru.vyarus.dropwizard.guice.module.jersey.GuiceFeature for integration details
 * @since 31.08.2014
 */
public class Jersey2Module extends ServletModule {

    /**
     * Guice filter registration name.
     */
    public static final String GUICE_FILTER = "Guice Filter";
    private static final String STAR = "*";
    private final Application application;
    private final Environment environment;

    public Jersey2Module(final Application application, final Environment environment) {
        this.application = application;
        this.environment = environment;
    }

    @Override
    protected void configureServlets() {
        // injector not available at this point, so using provider
        final InjectorProvider provider = new InjectorProvider(application);
        install(new GuiceBindingsModule(provider));
        final GuiceFeature component = new GuiceFeature(provider);
        bind(ServiceLocator.class).toProvider(component);
        environment.jersey().register(component);

        final GuiceFilter guiceFilter = new GuiceFilter();
        environment.servlets().addFilter(GUICE_FILTER, guiceFilter)
                .addMappingForUrlPatterns(null, false, environment.getApplicationContext().getContextPath() + STAR);
        environment.admin().addFilter(GUICE_FILTER, new AdminGuiceFilter(guiceFilter))
                .addMappingForUrlPatterns(null, false, environment.getAdminContext().getContextPath() + STAR);
    }
}

