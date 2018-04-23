package ru.vyarus.dropwizard.guice.module.lifecycle.event;

import com.google.inject.Injector;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo;
import ru.vyarus.dropwizard.guice.module.context.debug.report.diagnostic.DiagnosticConfig;
import ru.vyarus.dropwizard.guice.module.context.debug.report.diagnostic.DiagnosticRenderer;
import ru.vyarus.dropwizard.guice.module.context.debug.report.option.OptionsConfig;
import ru.vyarus.dropwizard.guice.module.context.debug.report.option.OptionsRenderer;
import ru.vyarus.dropwizard.guice.module.context.debug.report.stat.StatsRenderer;
import ru.vyarus.dropwizard.guice.module.context.debug.report.tree.ContextTreeConfig;
import ru.vyarus.dropwizard.guice.module.context.debug.report.tree.ContextTreeRenderer;
import ru.vyarus.dropwizard.guice.module.context.option.OptionsInfo;
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycle;

/**
 * Base class from events, started after guice injector creation. Since that moment, finalized guicey configuration
 * could be queued and diagnostic reporters could be used.
 *
 * @author Vyacheslav Rusakov
 * @since 19.04.2018
 */
public abstract class InjectorPhaseEvent extends RunPhaseEvent {

    private final Injector injector;
    private final ReportRenderer reportRenderer = new ReportRenderer();

    public InjectorPhaseEvent(final GuiceyLifecycle type,
                              final OptionsInfo options,
                              final Bootstrap bootstrap,
                              final Configuration configuration,
                              final Environment environment,
                              final Injector injector) {
        super(type, options, bootstrap, configuration, environment);
        this.injector = injector;
    }

    /**
     * @return guice injector instance
     */
    public Injector getInjector() {
        return injector;
    }

    /**
     * @return guicey configuration information
     */
    public GuiceyConfigurationInfo getConfigurationInfo() {
        return getInjector().getInstance(GuiceyConfigurationInfo.class);
    }

    /**
     * Renderers provide various views for guicey configuration, which may be used for specific logging.
     *
     * @return access to diagnostic reports renderers
     */
    public ReportRenderer getReportRenderer() {
        return reportRenderer;
    }

    /**
     * Guicey configuration reporting rendederers.
     */
    public class ReportRenderer {

        /**
         * Summary report render extensions by type. Useful for configuration overview.
         *
         * @param config config object
         * @return rendered report as string
         */
        public String renderConfigurationSummary(final DiagnosticConfig config) {
            return new DiagnosticRenderer(getConfigurationInfo()).renderReport(config);
        }

        /**
         * Tree report render extensions by configuration source. Useful for configuration sources understanding.
         *
         * @param config config object
         * @return rendered report as string
         */
        public String renderConfigurationTree(final ContextTreeConfig config) {
            return new ContextTreeRenderer(getConfigurationInfo()).renderReport(config);
        }

        /**
         * Options report.
         *
         * @param config config opbject
         * @return rendered report as string
         */
        public String renderOptions(final OptionsConfig config) {
            return new OptionsRenderer(getConfigurationInfo()).renderReport(config);
        }

        /**
         * Guicey timings report.
         *
         * @param hideTiny true to hide timers less then 1ms
         * @return rendered report as string
         */
        public String renderStats(final boolean hideTiny) {
            return new StatsRenderer(getConfigurationInfo()).renderReport(hideTiny);
        }
    }
}
