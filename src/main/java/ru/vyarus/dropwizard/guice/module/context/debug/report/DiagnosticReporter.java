package ru.vyarus.dropwizard.guice.module.context.debug.report;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vyarus.dropwizard.guice.module.context.debug.DiagnosticBundle;
import ru.vyarus.dropwizard.guice.module.context.debug.report.diagnostic.DiagnosticConfig;
import ru.vyarus.dropwizard.guice.module.context.debug.report.diagnostic.DiagnosticRenderer;
import ru.vyarus.dropwizard.guice.module.context.debug.report.option.OptionsConfig;
import ru.vyarus.dropwizard.guice.module.context.debug.report.option.OptionsRenderer;
import ru.vyarus.dropwizard.guice.module.context.debug.report.stat.StatsRenderer;
import ru.vyarus.dropwizard.guice.module.context.debug.report.tree.ContextTreeConfig;
import ru.vyarus.dropwizard.guice.module.context.debug.report.tree.ContextTreeRenderer;
import ru.vyarus.dropwizard.guice.module.installer.util.Reporter;

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
    private OptionsRenderer optionsRenderer;
    @Inject
    private DiagnosticRenderer diagnosticRenderer;
    @Inject
    private ContextTreeRenderer contextTreeRenderer;

    public void report(final String reportTitle,
                       final Boolean statsConfig,
                       final OptionsConfig optionsConfig,
                       final DiagnosticConfig config,
                       final ContextTreeConfig treeConfig) {

        final StringBuilder res = new StringBuilder(reportTitle);
        report("STARTUP STATS", statsRenderer, statsConfig, res);
        report("OPTIONS", optionsRenderer, optionsConfig, res);
        report("CONFIGURATION", diagnosticRenderer, config, res);
        report("CONFIGURATION TREE", contextTreeRenderer, treeConfig, res);
        logger.info(res.toString());
    }

    private <T> void report(final String name,
                            final ReportRenderer<T> renderer,
                            final T config,
                            final StringBuilder res) {
        if (config != null) {
            res.append(Reporter.NEWLINE).append(Reporter.NEWLINE)
                    .append("---------------------------------------------------------------------------[")
                    .append(name).append("]").append(renderer.renderReport(config));
        }
    }
}
