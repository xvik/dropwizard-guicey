package ru.vyarus.dropwizard.guice.module.context.debug;

import com.google.common.base.Preconditions;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import io.dropwizard.lifecycle.Managed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vyarus.dropwizard.guice.injector.lookup.InjectorLookup;
import ru.vyarus.dropwizard.guice.module.context.debug.diagnostic.DiagnosticConfig;
import ru.vyarus.dropwizard.guice.module.context.debug.diagnostic.DiagnosticRenderer;
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBootstrap;
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle;

/**
 * Bundle prints detailed configuration info.
 * Printed info:
 * <ul>
 * <li>Bundles tree</li>
 * <li>Installers and extensions in execution order</li>
 * <li>Not used and disabled installers</li>
 * <li>Registered guice modules</li>
 * </ul>
 * By default (when default bundle constructor used), configured to show most important info like bundles,
 * used extensions and registered modules.
 * <p>
 * To enable all options use:
 * <pre><code>
 *     new DiagnosticBundle(new DiagnosticConfig().printAll());
 * </code></pre>
 * <p>
 * Actual diagnostic rendering is performed by {@link DiagnosticRenderer}, and it may be used directly, for example,
 * to show report on web page.
 *
 * @author Vyacheslav Rusakov
 * @since 21.06.2016
 */
public class DiagnosticBundle implements GuiceyBundle {
    private final Logger logger = LoggerFactory.getLogger(DiagnosticBundle.class);

    private final DiagnosticConfig config;

    /**
     * Initialize bundle with default diagnostic configuration.
     */
    public DiagnosticBundle() {
        this(new DiagnosticConfig().printDefaults());
    }

    public DiagnosticBundle(final DiagnosticConfig config) {
        this.config = config;
        Preconditions.checkState(!config.isEmptyConfig(),
                "Empty config provided. Use at least one print option.");
    }

    @Override
    public void initialize(final GuiceyBootstrap bootstrap) {
        bootstrap.environment().lifecycle().manage(new Managed() {
            @Override
            public void start() throws Exception {
                final Injector injector = InjectorLookup.getInjector(bootstrap.application()).get();
                final DiagnosticRenderer diagnostic = injector.getInstance(DiagnosticRenderer.class);
                logger.info("Configuration diagnostic info = {}", diagnostic.renderReport(config));
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
        }
    }
}
