package ru.vyarus.dropwizard.guice.module.lifecycle.event;

import com.google.inject.Injector;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import ru.vyarus.dropwizard.guice.debug.report.diagnostic.DiagnosticConfig;
import ru.vyarus.dropwizard.guice.debug.report.diagnostic.DiagnosticRenderer;
import ru.vyarus.dropwizard.guice.debug.report.guice.GuiceAopConfig;
import ru.vyarus.dropwizard.guice.debug.report.guice.GuiceAopMapRenderer;
import ru.vyarus.dropwizard.guice.debug.report.guice.GuiceBindingsRenderer;
import ru.vyarus.dropwizard.guice.debug.report.guice.GuiceConfig;
import ru.vyarus.dropwizard.guice.debug.report.option.OptionsConfig;
import ru.vyarus.dropwizard.guice.debug.report.option.OptionsRenderer;
import ru.vyarus.dropwizard.guice.debug.report.stat.StatsRenderer;
import ru.vyarus.dropwizard.guice.debug.report.tree.ContextTreeConfig;
import ru.vyarus.dropwizard.guice.debug.report.tree.ContextTreeRenderer;
import ru.vyarus.dropwizard.guice.debug.report.yaml.BindingsConfig;
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo;
import ru.vyarus.dropwizard.guice.module.context.option.Options;
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycle;
import ru.vyarus.dropwizard.guice.module.yaml.ConfigurationTree;

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
                              final Options options,
                              final Bootstrap bootstrap,
                              final Configuration configuration,
                              final ConfigurationTree configurationTree,
                              final Environment environment,
                              final Injector injector) {
        super(type, options, bootstrap, configuration, configurationTree, environment);
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
     * @see #renderConfigurationBindings(BindingsConfig) for
     * configuration bindings report
     */
    public ReportRenderer getReportRenderer() {
        return reportRenderer;
    }

    /**
     * Guicey configuration reporting rendederers.
     * Does not include configuration bindings report because it's available long before these reports
     * (before guicey configuration process).
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
         * @param config config object
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

        /**
         * Render guice bindings report.
         *
         * @param config config object
         * @return rendered report as string
         */
        public String renderGuiceBindings(final GuiceConfig config) {
            return new GuiceBindingsRenderer(getInjector()).renderReport(config);
        }

        /**
         * Render guice aop map report.
         *
         * @param config config object
         * @return rendered report as string
         */
        public String renderGuiceAop(final GuiceAopConfig config) {
            return new GuiceAopMapRenderer(getInjector()).renderReport(config);
        }
    }
}
