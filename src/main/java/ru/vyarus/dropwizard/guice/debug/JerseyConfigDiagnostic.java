package ru.vyarus.dropwizard.guice.debug;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vyarus.dropwizard.guice.debug.report.jersey.JerseyConfig;
import ru.vyarus.dropwizard.guice.debug.report.jersey.JerseyConfigRenderer;
import ru.vyarus.dropwizard.guice.module.installer.InstallersOptions;
import ru.vyarus.dropwizard.guice.module.lifecycle.UniqueGuiceyLifecycleListener;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.jersey.ApplicationStartedEvent;

/**
 * Jersey configuration diagnostic report.
 * <p>
 * Must be registered with {@link ru.vyarus.dropwizard.guice.GuiceBundle.Builder#listen(
 * ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycleListener...)}.
 * Show all extension types by default, but may be configured to show only some types.
 * <p>
 * If multiple listeners registered, only first registered will be actually used (allow safe multiple registrations).
 *
 * @author Vyacheslav Rusakov
 * @since 26.10.2019
 */
public class JerseyConfigDiagnostic extends UniqueGuiceyLifecycleListener {
    private final Logger logger = LoggerFactory.getLogger(JerseyConfigDiagnostic.class);

    @Override
    protected void applicationStarted(final ApplicationStartedEvent event) {
        if (!event.isJettyStarted()) {
            // report can't be shown in lightweight tests (jersey injector not started)
            return;
        }
        final Boolean guiceFirstMode = event.getOptions().get(InstallersOptions.JerseyExtensionsManagedByGuice);
        final String report = new JerseyConfigRenderer(event.getInjectionManager(), guiceFirstMode)
                .renderReport(new JerseyConfig());
        logger.info("Jersey configuration = {}", report);
    }
}
