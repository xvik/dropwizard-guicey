package ru.vyarus.dropwizard.guice.module.context.debug.report;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vyarus.dropwizard.guice.module.context.debug.DiagnosticBundle;
import ru.vyarus.dropwizard.guice.module.context.debug.report.diagnostic.DiagnosticConfig;
import ru.vyarus.dropwizard.guice.module.context.debug.report.diagnostic.DiagnosticRenderer;
import ru.vyarus.dropwizard.guice.module.context.debug.report.stat.StatsRenderer;
import ru.vyarus.dropwizard.guice.module.context.debug.report.tree.ContextTreeConfig;
import ru.vyarus.dropwizard.guice.module.context.debug.report.tree.ContextTreeRenderer;

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

    @Inject
    private StatsRenderer statsRenderer;
    @Inject
    private DiagnosticRenderer diagnosticRenderer;
    @Inject
    private ContextTreeRenderer contextTreeRenderer;

    public void report(final Boolean statsConfig,
                       final DiagnosticConfig config,
                       final ContextTreeConfig treeConfig) {

        report("Startup stats = {}", statsRenderer, statsConfig);
        report("Configuration diagnostic info = {}", diagnosticRenderer, config);
        report("Configuration context tree = {}", contextTreeRenderer, treeConfig);
    }

    private <T> void report(final String name, final ReportRenderer<T> renderer, final T config) {
        if (config != null) {
            logger.info(name, renderer.renderReport(config));
        }
    }
}
