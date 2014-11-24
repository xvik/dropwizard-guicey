package ru.vyarus.dropwizard.guice.module.jersey;


import com.google.inject.servlet.GuiceFilter;
import com.google.inject.servlet.ServletModule;
import io.dropwizard.setup.Environment;
import org.glassfish.hk2.api.ServiceLocator;
import ru.vyarus.dropwizard.guice.module.jersey.hk2.GuiceBindingsModule;

/**
 * Guice jersey2 integration module.
 * <p>Integration is very similar to old jersey-guice: guice context is dominant;
 * guice instantiated first; jersey objects directly registered in guice; guice objects directly registered
 * in guice.</p>
 *
 * @author Vyacheslav Rusakov
 * @see ru.vyarus.dropwizard.guice.module.jersey.GuiceFeature for integration details
 * @since 31.08.2014
 */
public class Jersey2Module extends ServletModule {

    private final Environment environment;

    public Jersey2Module(final Environment environment) {
        this.environment = environment;
    }

    @Override
    protected void configureServlets() {
        install(new GuiceBindingsModule());
        final GuiceFeature component = new GuiceFeature();
        bind(ServiceLocator.class).toProvider(component);
        environment.jersey().register(component);

        environment.servlets().addFilter("Guice Filter", GuiceFilter.class)
                .addMappingForUrlPatterns(null, false, environment.getApplicationContext().getContextPath() + "*");
    }
}

