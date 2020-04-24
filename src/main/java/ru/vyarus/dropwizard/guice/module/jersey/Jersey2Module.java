package ru.vyarus.dropwizard.guice.module.jersey;


import com.google.common.base.Preconditions;
import com.google.inject.AbstractModule;
import com.google.inject.Stage;
import io.dropwizard.Application;
import io.dropwizard.setup.Environment;
import org.glassfish.jersey.internal.inject.InjectionManager;
import ru.vyarus.dropwizard.guice.injector.lookup.InjectorProvider;
import ru.vyarus.dropwizard.guice.module.context.ConfigurationContext;

import javax.servlet.DispatcherType;
import java.util.EnumSet;

import static ru.vyarus.dropwizard.guice.GuiceyOptions.GuiceFilterRegistration;
import static ru.vyarus.dropwizard.guice.GuiceyOptions.UseHkBridge;
import static ru.vyarus.dropwizard.guice.module.installer.InstallersOptions.JerseyExtensionsManagedByGuice;

/**
 * Guice jersey2 integration module.
 * <p>
 * Integration is very similar to old jersey-guice: guice context is dominant;
 * guice instantiated first; jersey objects directly registered in guice; guice objects directly registered
 * in HK2.
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
        checkHkFirstMode();
        final EnumSet<DispatcherType> types = context.option(GuiceFilterRegistration);
        final boolean guiceServletSupport = !types.isEmpty();

        // injector not available at this point, so using provider
        final InjectorProvider provider = new InjectorProvider(application);
        // todo not needed for guice
        //install(new GuiceBindingsModule(provider, guiceServletSupport));
        final GuiceFeature component =
                new GuiceFeature(provider, context.stat(), context.lifecycle());
        bind(InjectionManager.class).toProvider(component);
        // avoid registration when called within guice report
        if (currentStage() != Stage.TOOL) {
            environment.jersey().register(component);
        }

        if (guiceServletSupport) {
            install(new GuiceWebModule(environment, types));
        }
    }

    /**
     * When HK2 management for jersey extensions is enabled by default, then guice bridge must be enabled.
     * Without it guice beans could not be used in resources and other jersey extensions. If this is
     * expected then guice support is not needed at all.
     */
    private void checkHkFirstMode() {
        final boolean guiceyFirstMode = context.option(JerseyExtensionsManagedByGuice);
        if (!guiceyFirstMode) {
            Preconditions.checkState(context.option(UseHkBridge),
                    "HK2 management for jersey extensions is enabled by default "
                            + "(InstallersOptions.JerseyExtensionsManagedByGuice), but HK2-guice bridge is not "
                            + "enabled. Use GuiceyOptions.UseHkBridge option to enable bridge "
                            + "(extra dependency is required)");
        }
    }
}

