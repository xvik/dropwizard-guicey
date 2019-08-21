package ru.vyarus.dropwizard.guice.debug;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo;
import ru.vyarus.dropwizard.guice.debug.report.ReportRenderer;
import ru.vyarus.dropwizard.guice.debug.report.diagnostic.DiagnosticConfig;
import ru.vyarus.dropwizard.guice.debug.report.diagnostic.DiagnosticRenderer;
import ru.vyarus.dropwizard.guice.debug.report.option.OptionsConfig;
import ru.vyarus.dropwizard.guice.debug.report.option.OptionsRenderer;
import ru.vyarus.dropwizard.guice.debug.report.stat.StatsRenderer;
import ru.vyarus.dropwizard.guice.debug.report.tree.ContextTreeConfig;
import ru.vyarus.dropwizard.guice.debug.report.tree.ContextTreeRenderer;
import ru.vyarus.dropwizard.guice.module.installer.util.Reporter;
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycleAdapter;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.jersey.ApplicationStartedEvent;

/**
 * Guicey configuration diagnostic listener. Must be registered with
 * {@link ru.vyarus.dropwizard.guice.GuiceBundle.Builder#listen(
 *ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycleListener...)}. Prints detailed configuration info and
 * startup metrics.
 * <p>
 * Sections:
 * <ul>
 * <li>startup stats - collected timers and counters showing how much time were spent on different stages</li>
 * <li>options - used options</li>
 * <li>diagnostic section - summary of installed bundles, modules, used installers and extensions
 * (showing execution order). Shows what was configured.</li>
 * <li>context tree - tree showing configuration hierarchy (configuration sources). Shows
 * from where configuration parts come from.</li>
 * </ul>
 * <p>
 * Reporting is highly configurable. Default configuration shows most valuable (but not all possible) info.
 * To create bundle with custom configuration use builder:
 * <pre><code>
 *      ConfigurationDiagnostic.builder()
 *          .printStartupStats(true)
 *          .printConfiguration(new DiagnosticConfig().printAll())
 *          .build();
 * </code></pre>
 * <p>
 * Actual diagnostic rendering is performed by {@link DiagnosticRenderer}, {@link ContextTreeRenderer},
 * {@link OptionsRenderer} and {@link StatsRenderer}. They may be used directly, for example, to show report
 * on web page.
 * <p>
 * Reporting is performed after context startup (pure guicey context (in tests) or entire web context) and so
 * does not affect collected statistics (timings).
 * <p>
 * Only one listener with the same title will be registered to allow safe multiple registrations (de-duplication).
 *
 * @author Vyacheslav Rusakov
 * @since 16.08.2019
 */
@SuppressWarnings("checkstyle:ClassDataAbstractionCoupling")
public class ConfigurationDiagnostic extends GuiceyLifecycleAdapter {
    private final Logger logger = LoggerFactory.getLogger(ConfigurationDiagnostic.class);

    private final String reportTitle;
    private final Boolean statsConfig;
    private final OptionsConfig optionsConfig;
    private final DiagnosticConfig config;
    private final ContextTreeConfig treeConfig;

    /**
     * Initialize bundle with default diagnostic configuration. Configures most commonly required info.
     * Suitable for bundle usage with lookup mechanism.
     */
    public ConfigurationDiagnostic() {
        this(builder()
                .printStartupStats(true)

                .printOptions(new OptionsConfig()
                        .showNotDefinedOptions()
                        .showNotUsedMarker())

                .printConfiguration(new DiagnosticConfig()
                        .printDefaults())

                .printContextTree(new ContextTreeConfig()
                        .hideNotUsedInstallers()
                        .hideCommands()));
    }

    ConfigurationDiagnostic(final Builder builder) {
        this.reportTitle = builder.reportTitle;
        this.statsConfig = builder.statsConfig;
        this.optionsConfig = builder.optionsConfig;
        this.config = builder.config;
        this.treeConfig = builder.treeConfig;
    }

    @Override
    protected void applicationStarted(final ApplicationStartedEvent event) {
        final StringBuilder res = new StringBuilder(reportTitle);
        final GuiceyConfigurationInfo info = event.getConfigurationInfo();

        report("STARTUP STATS", new StatsRenderer(info), statsConfig, res);
        report("OPTIONS", new OptionsRenderer(info), optionsConfig, res);
        report("CONFIGURATION", new DiagnosticRenderer(info), config, res);
        report("CONFIGURATION TREE", new ContextTreeRenderer(info), treeConfig, res);

        logger.info(res.toString());
    }

    @Override
    public boolean equals(final Object obj) {
        // allow only one instance with the same title
        return obj instanceof ConfigurationDiagnostic
                && reportTitle.equals(((ConfigurationDiagnostic) obj).reportTitle);
    }

    @Override
    public int hashCode() {
        return reportTitle.hashCode();
    }

    public static Builder builder() {
        return new Builder("Diagnostic report");
    }

    /**
     * @param reportTitle report name for logs
     * @return builder for bundle configuration
     */
    public static Builder builder(final String reportTitle) {
        return new Builder(Preconditions.checkNotNull(reportTitle, "Report title required"));
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

    /**
     * Diagnostic bundle builder.
     */
    public static class Builder {

        private final String reportTitle;
        private Boolean statsConfig;
        private OptionsConfig optionsConfig;
        private DiagnosticConfig config;
        private ContextTreeConfig treeConfig;

        public Builder(String reportTitle) {
            this.reportTitle = reportTitle;
        }

        /**
         * Enables startup statistic reporting. Stats shows internal guicey timings and some details of configuration
         * process.
         * <p>
         * Enabled automatically if default bundle constructor used.
         *
         * @param hideSmallTimes true to hide times less then 1 ms, false to show everything
         * @return builder instance for chained calls
         * @see StatsRenderer
         */
        public Builder printStartupStats(final boolean hideSmallTimes) {
            this.statsConfig = hideSmallTimes;
            return this;
        }

        /**
         * Enables options reporting. Some options could be read lazily and so marked as NOT_USED at reporting time.
         *
         * @param config options section configuration
         * @return builder instance for chained calls
         */
        public Builder printOptions(final OptionsConfig config) {
            this.optionsConfig = config;
            return this;
        }

        /**
         * Enable configuration reporting. Shows configuration items in compact form. Suitable for
         * configuration overview.
         * <p>
         * In most situations default preset is enough:
         * <pre><code>
         *     new DiagnosticConfig().printDefaults();
         * </code></pre>
         *
         * @param config configuration reporting section configuration
         * @return builder instance for chained calls
         * @see DiagnosticRenderer
         */
        public Builder printConfiguration(final DiagnosticConfig config) {
            this.config = config;
            Preconditions.checkState(!config.isEmptyConfig(),
                    "Empty config provided. Use at least one print option.");
            return this;
        }

        /**
         * Enable context tree printing. Tree provides configuration sources perspective, suitable for better
         * understanding of configuration sources.
         * <p>
         * Note: in contrast to diagnostic config which is empty by default, tree config prints everything by default.
         *
         * @param treeConfig context tree section configuration
         * @return builder instance for chained calls
         * @see ContextTreeRenderer
         */
        public Builder printContextTree(final ContextTreeConfig treeConfig) {
            this.treeConfig = treeConfig;
            return this;
        }

        /**
         * @return configured listener instance
         */
        public ConfigurationDiagnostic build() {
            return new ConfigurationDiagnostic(this);
        }
    }
}
