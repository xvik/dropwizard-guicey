package ru.vyarus.dropwizard.guice.debug;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vyarus.dropwizard.guice.debug.report.web.MappingsConfig;
import ru.vyarus.dropwizard.guice.debug.report.web.WebMappingsRenderer;
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycleAdapter;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.jersey.ApplicationStartedEvent;

/**
 * Application web mappings listener. Prints all configured servlets and filters, including guice
 * {@link com.google.inject.servlet.ServletModule} declarations.
 *
 * @author Vyacheslav Rusakov
 * @since 22.10.2019
 */
public class WebMappingsDiagnostic extends GuiceyLifecycleAdapter {

    private final Logger logger = LoggerFactory.getLogger(WebMappingsDiagnostic.class);

    private final MappingsConfig config;

    public WebMappingsDiagnostic(final MappingsConfig config) {
        this.config = config;
    }

    @Override
    protected void applicationStarted(final ApplicationStartedEvent event) {
        logger.info("Web mappings: {}",
                new WebMappingsRenderer(event.getEnvironment(), event.getConfigurationInfo())
                        .renderReport(config));
    }

    @Override
    public boolean equals(final Object obj) {
        // allow only one reporter
        return obj instanceof WebMappingsDiagnostic;
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
