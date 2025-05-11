package ru.vyarus.dropwizard.guice.module.jersey;

import com.google.inject.Stage;
import com.google.inject.servlet.GuiceFilter;
import com.google.inject.servlet.ServletModule;
import io.dropwizard.core.setup.Environment;
import ru.vyarus.dropwizard.guice.module.installer.internal.AdminGuiceFilter;

import javax.servlet.DispatcherType;
import java.util.EnumSet;

/**
 * Registers {@link GuiceFilter} for application and admin scopes. Also, initializes request and session scopes
 * (as first registered servlet module).
 * <p>
 * Servlet modules support may be disabled, see
 * {@link ru.vyarus.dropwizard.guice.GuiceyOptions#GuiceFilterRegistration}.
 *
 * @author Vyacheslav Rusakov
 * @since 21.08.2016
 */
public class GuiceWebModule extends ServletModule {

    /**
     * Guice filter registration name.
     */
    public static final String GUICE_FILTER = "Guice Filter";
    private static final String ROOT_PATH = "/*";

    private final Environment environment;
    private final EnumSet<DispatcherType> dispatcherTypes;

    /**
     * Create web module.
     *
     * @param environment     environment
     * @param dispatcherTypes dispatcher types
     */
    public GuiceWebModule(final Environment environment, final EnumSet<DispatcherType> dispatcherTypes) {
        this.environment = environment;
        this.dispatcherTypes = dispatcherTypes;
    }

    @Override
    protected void configureServlets() {
        // avoid registrations for guice reports (performing modules analysis and so calling this code many times)
        if (currentStage() != Stage.TOOL) {
            final GuiceFilter guiceFilter = new GuiceFilter();
            environment.servlets().addFilter(GUICE_FILTER, guiceFilter)
                    .addMappingForUrlPatterns(dispatcherTypes, false, ROOT_PATH);
            environment.admin().addFilter(GUICE_FILTER, new AdminGuiceFilter(guiceFilter))
                    .addMappingForUrlPatterns(dispatcherTypes, false, ROOT_PATH);
        }
    }
}
