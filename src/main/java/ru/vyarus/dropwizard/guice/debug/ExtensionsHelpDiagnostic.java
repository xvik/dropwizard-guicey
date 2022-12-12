package ru.vyarus.dropwizard.guice.debug;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vyarus.dropwizard.guice.debug.report.extensions.ExtensionsHelpRenderer;
import ru.vyarus.dropwizard.guice.module.lifecycle.UniqueGuiceyLifecycleListener;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.configuration.InstallersResolvedEvent;

/**
 * Guicey extensions help: shows all extension recognition signs (for installers, providing this info).
 * <p>
 * In order to support this report, installer must implement
 * {@link ru.vyarus.dropwizard.guice.module.installer.FeatureInstaller#getRecognizableSigns()} (default interface
 * method).
 * <p>
 * If multiple reports would be registered only one instance would be accepted (to prevent report duplications).
 *
 * @author Vyacheslav Rusakov
 * @since 06.12.2022
 */
public class ExtensionsHelpDiagnostic extends UniqueGuiceyLifecycleListener {
    private final Logger logger = LoggerFactory.getLogger(ExtensionsHelpDiagnostic.class);

    @Override
    protected void installersResolved(final InstallersResolvedEvent event) {
        logger.warn("Recognized extension signs"
                + new ExtensionsHelpRenderer(event.getInstallers()).renderReport(null));
    }
}
