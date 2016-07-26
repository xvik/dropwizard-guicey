package ru.vyarus.dropwizard.guice.module.context.debug;

import com.google.common.base.Preconditions;
import com.google.inject.AbstractModule;
import io.dropwizard.lifecycle.Managed;
import ru.vyarus.dropwizard.guice.injector.lookup.InjectorLookup;
import ru.vyarus.dropwizard.guice.module.context.debug.diagnostic.DiagnosticConfig;
import ru.vyarus.dropwizard.guice.module.context.debug.diagnostic.DiagnosticRenderer;
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
 * Actual diagnostic rendering is performed by {@link DiagnosticRenderer} and {@link ContextTreeRenderer},
 * which may be used directly, for example, to show report on web page.
 *
 * @author Vyacheslav Rusakov
 * @since 21.06.2016
 */
public class DiagnosticBundle implements GuiceyBundle {

    private final DiagnosticConfig config;
    private ContextTreeConfig treeConfig;

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
                .hideEmptyBundles());
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

    @Override
    public void initialize(final GuiceyBootstrap bootstrap) {
        bootstrap.environment().lifecycle().manage(new Managed() {
            @Override
            public void start() throws Exception {
                final DiagnosticReporter reporter = new DiagnosticReporter(config, treeConfig);
                InjectorLookup.getInjector(bootstrap.application()).get().injectMembers(reporter);
                reporter.report();
            }

            @Override
            public void stop() throws Exception {
                // not needed
            }
        });
        bootstrap.modules(new DiagnosticModule());
    }

    /**
     * Guicey configuration diagnostic module.
     */
    public static class DiagnosticModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(DiagnosticRenderer.class);
            bind(ContextTreeRenderer.class);
        }
    }
}
