package ru.vyarus.dropwizard.guice.debug;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vyarus.dropwizard.guice.debug.report.web.MappingsConfig;
import ru.vyarus.dropwizard.guice.debug.report.web.WebMappingsRenderer;
import ru.vyarus.dropwizard.guice.module.lifecycle.UniqueGuiceyLifecycleListener;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.jersey.ApplicationStartedEvent;

/**
 * Application web mappings listener. Prints all configured servlets and filters, including guice
 * {@link com.google.inject.servlet.ServletModule} declarations.
 * <p>
 * Must be registered with {@link ru.vyarus.dropwizard.guice.GuiceBundle.Builder#listen(
 * ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycleListener...)}.
 * Should be configured what info to show because by default nothing is shown.
 * <p>
 * If multiple listeners registered, only first registered will be actually used (allow safe multiple registrations).
 *
 * @author Vyacheslav Rusakov
 * @since 22.10.2019
 */
public class WebMappingsDiagnostic extends UniqueGuiceyLifecycleListener {

    private final Logger logger = LoggerFactory.getLogger(WebMappingsDiagnostic.class);

    private final MappingsConfig config;

    public WebMappingsDiagnostic(final MappingsConfig config) {
        this.config = config;
    }

    @Override
    protected void applicationStarted(final ApplicationStartedEvent event) {
        logger.warn("Web mappings: {}",
                new WebMappingsRenderer(event.getEnvironment(), event.getConfigurationInfo())
                        .renderReport(config));
    }
}
