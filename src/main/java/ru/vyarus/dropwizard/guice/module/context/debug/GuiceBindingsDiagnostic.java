package ru.vyarus.dropwizard.guice.module.context.debug;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vyarus.dropwizard.guice.module.context.debug.report.guice.GuiceBindingsRenderer;
import ru.vyarus.dropwizard.guice.module.context.debug.report.guice.GuiceConfig;
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycleAdapter;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.jersey.ApplicationStartedEvent;

/**
 * Guice bindings debug listener. Must be registered with
 * {@link ru.vyarus.dropwizard.guice.GuiceBundle.Builder#listen(
 *ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycleListener...)}.
 * Could be configured to filter out not required info.
 * <p>
 * If multiple listeners registered, only first registered will be actually used (allow safe multiple registrations).
 *
 * @author Vyacheslav Rusakov
 * @since 13.08.2019
 */
public class GuiceBindingsDiagnostic extends GuiceyLifecycleAdapter {

    private final Logger logger = LoggerFactory.getLogger(GuiceBindingsDiagnostic.class);
    private GuiceConfig config;

    public GuiceBindingsDiagnostic(final GuiceConfig config) {
        this.config = config;
    }

    @Override
    protected void applicationStarted(final ApplicationStartedEvent event) {
        final String report = new GuiceBindingsRenderer(event.getInjector()).renderReport(config);
        logger.info("Guice bindings = {}", report);
    }

    @Override
    public boolean equals(final Object obj) {
        // allow only one reporter
        return obj instanceof GuiceBindingsDiagnostic;
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
