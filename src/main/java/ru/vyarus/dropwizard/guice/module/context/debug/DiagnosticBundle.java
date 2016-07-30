package ru.vyarus.dropwizard.guice.module.context.debug;

import com.google.common.base.Preconditions;
import com.google.inject.AbstractModule;
import io.dropwizard.Application;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.component.LifeCycle;
import ru.vyarus.dropwizard.guice.injector.lookup.InjectorLookup;
import ru.vyarus.dropwizard.guice.module.context.debug.diagnostic.DiagnosticConfig;
import ru.vyarus.dropwizard.guice.module.context.debug.diagnostic.DiagnosticRenderer;
import ru.vyarus.dropwizard.guice.module.context.debug.stat.StatsRenderer;
import ru.vyarus.dropwizard.guice.module.context.debug.tree.ContextTreeConfig;
import ru.vyarus.dropwizard.guice.module.context.debug.tree.ContextTreeRenderer;
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBootstrap;
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle;

/**
 * Bundle prints detailed configuration info. May be used with bundle lookup mechanism (for example, enable it
 * with system property).
 * <p>
 * Sections:
 * <ul>
 * <li>startup stats - collected timers and counters showing how much time were spent on different stages</li>
 * <li>diagnostic section - summary of installed bundles, modules, used installers and extensions
 * (showing execution order). Shows what was configured.</li>
 * <li>context tree - tree showing configuration hierarchy (configuration sources). Shows
 * from where configuration parts come from.</li>
 * </ul>
 * <p>
 * By default (when default bundle constructor used), configured to show most important info in all sections
 * (but each section could show more info if configured manually): reporting is highly configurable.
 * <p>
 * To configure main diagnostics section use custom constructor:
 * <pre><code>
 *     new DiagnosticBundle(new DiagnosticConfig().printAll());
 * </code></pre>
 * NOTE: using non default constructor will not enable context tree rendering.
 * <p>
 * To configure context tree section, use {@link #printContextTree(ContextTreeConfig)}:
 * <pre><code>
 *     new DiagnosticBundle(new DiagnosticConfig().printAll())
 *          .printContextTree(new ContextTreeConfig().hideDuplicateRegistrations());
 * </code></pre>
 * <p>
 * To print startup stats use {@link #printStartupStats()}:
 * <pre><code>
 *     new DiagnosticBundle(new DiagnosticConfig().printAll())
 *          .printStartupStats()
 * </code></pre>
 * <p>
 * Actual diagnostic rendering is performed by {@link DiagnosticRenderer}, {@link ContextTreeRenderer} and
 * {@link StatsRenderer}. They may be used directly, for example, to show report on web page.
 * <p>
 * Reporting is performed after context startup (pure guicey context (in tests) or entire web context) and so
 * does not affect collected statistics.
 *
 * @author Vyacheslav Rusakov
 * @since 21.06.2016
 */
public final class DiagnosticBundle implements GuiceyBundle {

    private final DiagnosticConfig config;
    private ContextTreeConfig treeConfig;
    private boolean printStats;

    /**
     * Initialize bundle with default diagnostic configuration. Configures most commonly required info.
     * Suitable for bundle usage with lookup mechanism.
     */
    public DiagnosticBundle() {
        this(new DiagnosticConfig()
                .printDefaults());

        printContextTree(new ContextTreeConfig()
                .hideDuplicateRegistrations()
                .hideNotUsedInstallers()
                .hideEmptyBundles()
                .hideCommands());

        printStartupStats();
    }

    /**
     * Use to override default diagnostic configuration.
     * Note: in contrast to default constructor, this one will only enable diagnostic section. Context tree must
     * be enabled manually using {@link #printContextTree(ContextTreeConfig)} method.
     *
     * @param config diagnostic section configuration
     */
    public DiagnosticBundle(final DiagnosticConfig config) {
        this.config = config;
        Preconditions.checkState(!config.isEmptyConfig(),
                "Empty config provided. Use at least one print option.");
    }

    /**
     * Enable context tree printing. Tree provides configuration sources perspective, suitable for better
     * understanding of configuration sources.
     * <p>
     * Note: in contrast to diagnostic config which is empty by default, tree config prints everything by default.
     *
     * @param treeConfig context tree section configuration
     * @return bundle instance
     */
    public DiagnosticBundle printContextTree(final ContextTreeConfig treeConfig) {
        this.treeConfig = treeConfig;
        return this;
    }

    /**
     * Enables startup statistic printing. Stats shows internal guicey timings and some details of configuration
     * process.
     * <p>
     * Enabled automatically if default bundle constructor used.
     *
     * @return bundle instance
     */
    public DiagnosticBundle printStartupStats() {
        this.printStats = true;
        return this;
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
        final DiagnosticReporter reporter = new DiagnosticReporter(config, treeConfig, printStats);
        InjectorLookup.getInjector(app).get().injectMembers(reporter);
        reporter.report();
    }

    /**
     * Guicey configuration diagnostic module.
     */
    public static class DiagnosticModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(StatsRenderer.class);
            bind(DiagnosticRenderer.class);
            bind(ContextTreeRenderer.class);
        }
    }
}
