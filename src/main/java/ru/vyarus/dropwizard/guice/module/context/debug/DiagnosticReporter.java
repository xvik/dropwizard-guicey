package ru.vyarus.dropwizard.guice.module.context.debug;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vyarus.dropwizard.guice.module.context.debug.diagnostic.DiagnosticConfig;
import ru.vyarus.dropwizard.guice.module.context.debug.diagnostic.DiagnosticRenderer;
import ru.vyarus.dropwizard.guice.module.context.debug.tree.ContextTreeConfig;
import ru.vyarus.dropwizard.guice.module.context.debug.tree.ContextTreeRenderer;

import javax.inject.Inject;

/**
 * Logs diagnostic info, configured by {@link DiagnosticBundle}. Not intended to be managed by guice, but
 * requires guice member injection.
 *
 * @author Vyacheslav Rusakov
 * @since 25.07.2016
 */
public final class DiagnosticReporter {
    private final Logger logger = LoggerFactory.getLogger(DiagnosticReporter.class);

    private final DiagnosticConfig config;
    private final ContextTreeConfig treeConfig;

    @Inject
    private DiagnosticRenderer diagnosticRenderer;
    @Inject
    private ContextTreeRenderer contextTreeRenderer;

    public DiagnosticReporter(final DiagnosticConfig config, final ContextTreeConfig treeConfig) {
        this.config = config;
        this.treeConfig = treeConfig;
    }

    public void report() {
        logger.info("Configuration diagnostic info = {}", diagnosticRenderer.renderReport(config));
        if (treeConfig != null) {
            logger.info("Configuration context tree = {}", contextTreeRenderer.renderReport(treeConfig));
        }
    }
}
