package ru.vyarus.dropwizard.guice.debug;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vyarus.dropwizard.guice.debug.report.jersey.JerseyConfigRenderer;
import ru.vyarus.dropwizard.guice.module.installer.InstallersOptions;
import ru.vyarus.dropwizard.guice.module.lifecycle.UniqueGuiceyLifecycleListener;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.jersey.ApplicationStartedEvent;

/**
 * Jersey configuration diagnostic report.
 *
 * @author Vyacheslav Rusakov
 * @since 26.10.2019
 */
public class JerseyConfigDiagnostic extends UniqueGuiceyLifecycleListener {
    private final Logger logger = LoggerFactory.getLogger(JerseyConfigDiagnostic.class);

    @Override
    protected void applicationStarted(final ApplicationStartedEvent event) {
        final String report = new JerseyConfigRenderer(event.getInjectionManager(),
                event.getOptions().get(InstallersOptions.JerseyExtensionsManagedByGuice)).renderReport(null);
        logger.info("Jersey configuration = {}", report);
    }
}
