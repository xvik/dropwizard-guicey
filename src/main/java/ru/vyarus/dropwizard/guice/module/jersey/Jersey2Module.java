package ru.vyarus.dropwizard.guice.module.jersey;


import com.google.inject.AbstractModule;
import io.dropwizard.Application;
import io.dropwizard.setup.Environment;
import org.glassfish.hk2.api.ServiceLocator;
import ru.vyarus.dropwizard.guice.injector.lookup.InjectorProvider;
import ru.vyarus.dropwizard.guice.module.context.ConfigurationContext;
import ru.vyarus.dropwizard.guice.module.jersey.hk2.GuiceBindingsModule;

import javax.servlet.DispatcherType;
import java.util.EnumSet;

import static ru.vyarus.dropwizard.guice.GuiceyOptions.UseHkBridge;
import static ru.vyarus.dropwizard.guice.GuiceyOptions.GuiceFilterRegistration;

/**
 * Guice jersey2 integration module.
 * <p>
 * Integration is very similar to old jersey-guice: guice context is dominant;
 * guice instantiated first; jersey objects directly registered in guice; guice objects directly registered
 * in hk.
 * <p>
 * Guice {@link com.google.inject.servlet.ServletModule} support is optional and may be disabled, see
 * {@link ru.vyarus.dropwizard.guice.GuiceyOptions#GuiceFilterRegistration}.
 *
 * @author Vyacheslav Rusakov
 * @see ru.vyarus.dropwizard.guice.module.jersey.GuiceFeature for integration details
 * @since 31.08.2014
 */
public class Jersey2Module extends AbstractModule {

    private final Application application;
    private final Environment environment;
    private final ConfigurationContext context;

    public Jersey2Module(final Application application, final Environment environment,
                         final ConfigurationContext context) {
        this.application = application;
        this.environment = environment;
        this.context = context;
    }

    @Override
    protected void configure() {
        final EnumSet<DispatcherType> types = context.option(GuiceFilterRegistration);
        final boolean guiceServletSupport = !types.isEmpty();

        // injector not available at this point, so using provider
        final InjectorProvider provider = new InjectorProvider(application);
        install(new GuiceBindingsModule(provider, guiceServletSupport));
        final GuiceFeature component =
                new GuiceFeature(provider, context.stat(), context.lifecycle(), context.option(UseHkBridge));
        bind(ServiceLocator.class).toProvider(component);
        environment.jersey().register(component);

        if (guiceServletSupport) {
            install(new GuiceWebModule(environment, types));
        }
    }
}

