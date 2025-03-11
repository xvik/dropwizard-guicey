package ru.vyarus.dropwizard.guice.debug;

import com.google.common.base.Stopwatch;
import org.eclipse.jetty.util.component.LifeCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vyarus.dropwizard.guice.debug.report.start.DropwizardBundlesTracker;
import ru.vyarus.dropwizard.guice.debug.report.start.ManagedTracker;
import ru.vyarus.dropwizard.guice.debug.report.start.ShutdownTimeInfo;
import ru.vyarus.dropwizard.guice.debug.report.start.ShutdownTimeRenderer;
import ru.vyarus.dropwizard.guice.debug.report.start.StartupTimeInfo;
import ru.vyarus.dropwizard.guice.debug.report.start.StartupTimeRenderer;
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo;
import ru.vyarus.dropwizard.guice.module.lifecycle.UniqueGuiceyLifecycleListener;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.configuration.BeforeInitEvent;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.run.ApplicationRunEvent;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.run.BeforeRunEvent;

/**
 * Startup time report. Timers could count only time AFTER guice bundle creation (with guice bundle itself).
 * <p>
 * Report hacks dropwizard bundles and managed objects to track all bundles time. Note that it is impossible to
 * track init time of bundles registered before guice bundle.
 * <p>
 * Entire application startup time (measured since guice bundle creation) is split into 3 chunks:
 *  - init phase - all dropwizard bundle init methods called
 *  - run phase - all dropwizard bundles run methods called (+ time of Configuration and Environment creation);
 *      also includes application init method
 *  - web phase - everything after last dropwizard bundle run (including application run method)
 *  <p>
 *  To avoid confusion with server startup time - jvm start time is also shown (time from jvm start and before
 *  guice bundle creation).
 *
 * @author Vyacheslav Rusakov
 * @since 07.03.2025
 */
public class StartupTimeDiagnostic extends UniqueGuiceyLifecycleListener {
    private final Logger logger = LoggerFactory.getLogger(StartupTimeDiagnostic.class);

    private final StartupTimeInfo start = new StartupTimeInfo();
    private final ShutdownTimeInfo stop = new ShutdownTimeInfo();

    // for tracking dw phases in bundles tracker
    private DropwizardBundlesTracker bundlesTracker;

    @Override
    protected void beforeInit(final BeforeInitEvent event) {
        bundlesTracker = new DropwizardBundlesTracker(event.getStats(), start, event.getBootstrap());
    }

    // just before guicey run
    @Override
    protected void beforeRun(final BeforeRunEvent event) {
        // for tracking managed objects execution
        new ManagedTracker(start, stop, event.getEnvironment().lifecycle());
    }

    // guicey run done
    @Override
    @SuppressWarnings("AnonInnerLength")
    protected void applicationRun(final ApplicationRunEvent event) {
        // apply custom listener instead of guicey events to run AFTER all guicey events
        event.getEnvironment().lifecycle().addEventListener(new LifeCycle.Listener() {

            private final Stopwatch startTime = Stopwatch.createUnstarted();
            private final Stopwatch stopTime = Stopwatch.createUnstarted();

            @Override
            public void lifeCycleStarting(final LifeCycle event) {
                startTime.start();
            }

            @Override
            public void lifeCycleStarted(final LifeCycle evt) {
                // web time - everything after last bundle run
                // OverallTime stat will get called a bit earlier (cause listener registered earlier, so using custom
                // timer for more accurate value)
                start.setWebTime(bundlesTracker.getWebTimer().stop().elapsed());
                start.setLifecycleTime(startTime.elapsed());
                start.setStats(event.getInjector().getInstance(GuiceyConfigurationInfo.class).getStats());
                logger.info("Application startup time: {}", new StartupTimeRenderer().render(start));
            }

            @Override
            public void lifeCycleStopping(final LifeCycle event) {
                stopTime.start();
            }

            @Override
            public void lifeCycleStopped(final LifeCycle event) {
                stop.setStopTime(stopTime.stop().elapsed());
                logger.info("Application shutdown time: {}", new ShutdownTimeRenderer().render(stop));
            }
        });
    }
}
