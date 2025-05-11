package ru.vyarus.dropwizard.guice.debug;

import com.google.common.base.Stopwatch;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import com.google.inject.AbstractModule;
import com.google.inject.Binding;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.ProvisionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vyarus.dropwizard.guice.debug.report.guice.GuiceProvisionRenderer;
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle;
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyEnvironment;

import java.time.Duration;

/**
 * Guice beans creation (provision) time diagnostic. Records all requested beans creation time. By default,
 * prints collected data after application startup (most of the beans created at startup).
 * <p>
 * The report shows:
 * <ul>
 *  <li>Each bean creation time (mostly useful for providers, which might be slow)
 *  <li>Number of created instances (for each type)
 *  <li>All requested bindings, including JIT bindings - useful for unexpected JIT bindings detection
 *  <li>Detects if the same type is requested with and without qualifier - usually this means incorrect usage
 *  (forgotten qualifier), but it might not be obvious (as guice create JIT binding in this case).
 * </ul>
 * The report is sorted by overall spent time: if guice bean (in prototype) scope was created several times - the summ
 * of all creations is counted.
 * <p>
 * The report also could be used to measure runtime creations:
 * <pre><code>
 *     // false to avoid startup report
 *     GuiceProvisionDiagnostic report = new GuiceProvisionDiagnostic(false);
 *     // registre report
 *     GuiceBundle....bundles(report);
 *
 *     // clear collected data before required point
 *     report.clear();
 *     // do something requiring new beans creation
 *     injector.getInstance(JitService.class); // just an example
 *
 *     // generate report after measured actions
 *     logger.info("Guice provision time {}", report.renderReport());
 * </code></pre>
 *
 * @author Vyacheslav Rusakov
 * @since 24.03.2025
 */
public class GuiceProvisionDiagnostic implements GuiceyBundle {
    private final Logger logger = LoggerFactory.getLogger(GuiceProvisionDiagnostic.class);
    private final ListMultimap<Binding<?>, Duration> data = LinkedListMultimap.create();

    private final boolean printStartupReport;

    /**
     * Create report.
     *
     * @param printStartupReport true to print report after application startup
     */
    public GuiceProvisionDiagnostic(final boolean printStartupReport) {
        this.printStartupReport = printStartupReport;
    }

    @Override
    public void run(final GuiceyEnvironment environment) throws Exception {
        environment.modules(new ProvisionListenerModule(data));
        if (printStartupReport) {
            environment.onApplicationStartup(injector ->
                    logger.info("Guice bindings provision time: {}", renderReport()));
        }
    }

    /**
     * Clear collected data (to record new data at runtime).
     */
    public void clear() {
        data.clear();
    }

    /**
     * Map format: binding - provisions time.
     *
     * @return recorded provision data
     */
    public ListMultimap<Binding<?>, Duration> getRecordedData() {
        return LinkedListMultimap.create(data);
    }

    /**
     * @return generated report for collected data
     */
    public String renderReport() {
        return new GuiceProvisionRenderer().render(LinkedListMultimap.create(data));
    }

    /**
     * Module records guice beans provision times.
     */
    public static class ProvisionListenerModule extends AbstractModule {

        private final Multimap<Binding<?>, Duration> provisions;

        /**
         * Create module.
         *
         * @param provisions provisions collector
         */
        public ProvisionListenerModule(final Multimap<Binding<?>, Duration> provisions) {
            this.provisions = provisions;
        }

        @Override
        protected void configure() {
            bindListener(Matchers.any(), new ProvisionListener() {
                @Override
                public <T> void onProvision(final ProvisionInvocation<T> provision) {
                    final Stopwatch stopwatch = Stopwatch.createStarted();
                    provision.provision();
                    provisions.put(provision.getBinding(), stopwatch.stop().elapsed());
                }
            });
        }
    }
}
