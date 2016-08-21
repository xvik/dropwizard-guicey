package ru.vyarus.dropwizard.guice.module.context.debug;

import com.google.common.base.Preconditions;
import com.google.inject.AbstractModule;
import io.dropwizard.Application;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.component.LifeCycle;
import ru.vyarus.dropwizard.guice.injector.lookup.InjectorLookup;
import ru.vyarus.dropwizard.guice.module.context.debug.report.DiagnosticReporter;
import ru.vyarus.dropwizard.guice.module.context.debug.report.diagnostic.DiagnosticConfig;
import ru.vyarus.dropwizard.guice.module.context.debug.report.diagnostic.DiagnosticRenderer;
import ru.vyarus.dropwizard.guice.module.context.debug.report.option.OptionsConfig;
import ru.vyarus.dropwizard.guice.module.context.debug.report.option.OptionsRenderer;
import ru.vyarus.dropwizard.guice.module.context.debug.report.stat.StatsRenderer;
import ru.vyarus.dropwizard.guice.module.context.debug.report.tree.ContextTreeConfig;
import ru.vyarus.dropwizard.guice.module.context.debug.report.tree.ContextTreeRenderer;
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBootstrap;
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle;

/**
 * Bundle prints detailed configuration info and startup metrics.
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
 *      DiagnosticBundle.builder()
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
 * does not affect collected statistics.
 *
 * @author Vyacheslav Rusakov
 * @since 21.06.2016
 */
public class DiagnosticBundle implements GuiceyBundle {

    private final Boolean statsConfig;
    private final OptionsConfig optionsConfig;
    private final DiagnosticConfig config;
    private final ContextTreeConfig treeConfig;

    /**
     * Initialize bundle with default diagnostic configuration. Configures most commonly required info.
     * Suitable for bundle usage with lookup mechanism.
     */
    public DiagnosticBundle() {
        this(builder()
                .printStartupStats(true)

                .printOptions(new OptionsConfig()
                        .showNotDefinedOptions()
                        .showNotUsedMarker())

                .printConfiguration(new DiagnosticConfig()
                        .printDefaults())

                .printContextTree(new ContextTreeConfig()
                        .hideNotUsedInstallers()
                        .hideEmptyBundles()
                        .hideCommands()));
    }

    DiagnosticBundle(final Builder builder) {
        this.statsConfig = builder.statsConfig;
        this.optionsConfig = builder.optionsConfig;
        this.config = builder.config;
        this.treeConfig = builder.treeConfig;
    }

    @Override
    public void initialize(final GuiceyBootstrap bootstrap) {
        // use  listener to work properly for both guicey only test and normal app (to show hk stats)
        bootstrap.environment().lifecycle().addLifeCycleListener(new AbstractLifeCycle.AbstractLifeCycleListener() {
            @Override
            public void lifeCycleStarted(final LifeCycle event) {
                report(bootstrap.application());
            }
        });
        bootstrap.modules(new DiagnosticModule());
    }

    private void report(final Application app) {
        final DiagnosticReporter reporter = new DiagnosticReporter();
        InjectorLookup.getInjector(app).get().injectMembers(reporter);
        reporter.report(statsConfig, optionsConfig, config, treeConfig);
    }

    /**
     * @return builder for bundle configuration
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Diagnostic bundle builder.
     */
    public static class Builder {

        private Boolean statsConfig;
        private OptionsConfig optionsConfig;
        private DiagnosticConfig config;
        private ContextTreeConfig treeConfig;

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
         * @return configured bundle instance
         */
        public DiagnosticBundle build() {
            return new DiagnosticBundle(this);
        }
    }

    /**
     * Guicey configuration diagnostic module.
     */
    public static class DiagnosticModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(StatsRenderer.class);
            bind(OptionsRenderer.class);
            bind(DiagnosticRenderer.class);
            bind(ContextTreeRenderer.class);
        }
    }
}
