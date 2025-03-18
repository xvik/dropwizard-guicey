package ru.vyarus.dropwizard.guice.debug;

import com.google.common.base.Stopwatch;
import org.eclipse.jetty.util.component.LifeCycle;
import org.glassfish.jersey.server.monitoring.ApplicationEvent;
import org.glassfish.jersey.server.monitoring.ApplicationEventListener;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vyarus.dropwizard.guice.debug.report.start.DropwizardBundlesTracker;
import ru.vyarus.dropwizard.guice.debug.report.start.ManagedTracker;
import ru.vyarus.dropwizard.guice.debug.report.start.ShutdownTimeInfo;
import ru.vyarus.dropwizard.guice.debug.report.start.ShutdownTimeRenderer;
import ru.vyarus.dropwizard.guice.debug.report.start.StartupTimeInfo;
import ru.vyarus.dropwizard.guice.debug.report.start.StartupTimeRenderer;
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo;
import ru.vyarus.dropwizard.guice.module.context.ConfigItem;
import ru.vyarus.dropwizard.guice.module.context.info.ItemId;
import ru.vyarus.dropwizard.guice.module.context.stat.DetailStat;
import ru.vyarus.dropwizard.guice.module.context.stat.Stat;
import ru.vyarus.dropwizard.guice.module.context.stat.StatsInfo;
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle;
import ru.vyarus.dropwizard.guice.module.lifecycle.UniqueGuiceyLifecycleListener;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.configuration.BeforeInitEvent;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.configuration.BundlesInitializedEvent;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.run.ApplicationRunEvent;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.run.BeforeRunEvent;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
@SuppressWarnings({"ClassDataAbstractionCoupling", "ClassFanOutComplexity", "PMD.ExcessiveImports"})
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

    @Override
    protected void bundlesInitialized(final BundlesInitializedEvent event) {
        // store bundles init order to show them correctly
        start.setGuiceyBundlesInitOrder(event.getBundles().stream()
                .map(GuiceyBundle::getClass)
                .collect(Collectors.toList()));
    }

    // just before guicey run
    @Override
    protected void beforeRun(final BeforeRunEvent event) {
        // for tracking managed objects execution
        new ManagedTracker(start, stop, event.getEnvironment().lifecycle());
        final Stopwatch jerseyTime = Stopwatch.createUnstarted();
        // listener would be called on normal run and for grizzly 2 rest stubs
        event.getEnvironment().jersey().register(new ApplicationEventListener() {
            @Override
            public void onEvent(final ApplicationEvent applicationEvent) {
                final ApplicationEvent.Type type = applicationEvent.getType();
                if (type == ApplicationEvent.Type.INITIALIZATION_START) {
                    jerseyTime.start();
                } else if (type == ApplicationEvent.Type.INITIALIZATION_FINISHED) {
                    start.setJerseyTime(jerseyTime.elapsed());
                }
            }

            @Override
            public RequestEventListener onRequest(final RequestEvent requestEvent) {
                return null;
            }
        });
    }

    // guicey run done
    @Override
    @SuppressWarnings("AnonInnerLength")
    protected void applicationRun(final ApplicationRunEvent event) {
        // remember transitive bundles to correctly calculate each bundle time
        event.getConfigurationInfo().getGuiceyBundleIds().forEach(id -> {
            final List<Class<?>> transitive = event.getConfigurationInfo().getData().getItems(itemInfo ->
                    ConfigItem.Bundle.equals(itemInfo.getItemType()) && itemInfo.getRegistrationScope().equals(id))
                    .stream().map(ItemId::getType).collect(Collectors.toList());
            if (!transitive.isEmpty()) {
                start.getGuiceyBundleTransitives().putAll(id.getType(), transitive);
            }
        });
        // apply custom listener instead of guicey events to run AFTER all guicey events
        event.registerJettyListener(new JettyListener(event));
    }

    private class JettyListener implements LifeCycle.Listener {

        private final Stopwatch startTime;
        private final Stopwatch stopTime;
        private final List<Class<?>> startupEvents;
        private final ApplicationRunEvent event;
        private Duration startListenersTime;

        JettyListener(final ApplicationRunEvent event) {
            this.event = event;
            startTime = Stopwatch.createUnstarted();
            stopTime = Stopwatch.createUnstarted();
            startupEvents = new ArrayList<>();
        }

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
            final StatsInfo stats = event.getInjector().getInstance(GuiceyConfigurationInfo.class).getStats();
            start.setStats(stats);
            startupEvents.addAll(stats.getDetailedStats(DetailStat.Listener).keySet());
            startListenersTime = stats.duration(Stat.ListenersTime);
            start.getWebEvents().addAll(startupEvents);
            start.getWebEvents().removeAll(start.getInitEvents());
            start.getWebEvents().removeAll(start.getRunEvents());
            logger.info("Application startup time: {}", new StartupTimeRenderer().render(start));
        }

        @Override
        public void lifeCycleStopping(final LifeCycle event) {
            stopTime.start();
        }

        @Override
        public void lifeCycleStopped(final LifeCycle event) {
            stop.setStopTime(stopTime.stop().elapsed());
            stop.getEvents().addAll(start.getStats().getDetailedStats(DetailStat.Listener).keySet());
            stop.getEvents().removeAll(startupEvents);
            stop.setListenersTime(start.getStats().duration(Stat.ListenersTime).minus(startListenersTime));
            stop.setStats(start.getStats());
            logger.info("Application shutdown time: {}", new ShutdownTimeRenderer().render(stop));
        }
    }
}
