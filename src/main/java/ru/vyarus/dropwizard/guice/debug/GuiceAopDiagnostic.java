package ru.vyarus.dropwizard.guice.debug;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vyarus.dropwizard.guice.debug.report.guice.GuiceAopConfig;
import ru.vyarus.dropwizard.guice.debug.report.guice.GuiceAopMapRenderer;
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycleAdapter;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.jersey.ApplicationStartedEvent;

/**
 * Guice AOP map debug listener. Must be registered with
 * {@link ru.vyarus.dropwizard.guice.GuiceBundle.Builder#listen(
 *ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycleListener...)}.
 * Could be configured to filter out not required info.
 *
 * @author Vyacheslav Rusakov
 * @since 23.08.2019
 */
public class GuiceAopDiagnostic extends GuiceyLifecycleAdapter {

    private final Logger logger = LoggerFactory.getLogger(GuiceAopDiagnostic.class);
    private GuiceAopConfig config;

    public GuiceAopDiagnostic(final GuiceAopConfig config) {
        this.config = config;
    }

    @Override
    protected void applicationStarted(final ApplicationStartedEvent event) {
        final String report = new GuiceAopMapRenderer(event.getInjector()).renderReport(config);
        logger.info("Guice AOP map = {}", report);
    }
}
