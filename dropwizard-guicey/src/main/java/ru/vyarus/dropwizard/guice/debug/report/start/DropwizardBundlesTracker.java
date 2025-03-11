package ru.vyarus.dropwizard.guice.debug.report.start;

import com.google.common.base.Stopwatch;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.ConfiguredBundle;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vyarus.dropwizard.guice.module.context.stat.Stat;
import ru.vyarus.dropwizard.guice.module.context.stat.StatsInfo;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Dropwizard bundles tracker for startup time report. Replaces bundles list inside
 * {@link io.dropwizard.core.setup.Bootstrap} object to detect bundles addition and wrap them to track
 * run phase. Note that bundle added to collection AFTER initialization call. Also, bundle may add other bundles,
 * which initialization would be called immediately and so it is impossible to measure initialization time
 * of each bundle. So, instead of measuring bundle init time, bundle init done point is registered (it should
 * be enough to highlight slow bundles).
 * <p>
 * It is not possible to measure init time for bundles, registered before the guice bundle. Warning would be shown.
 *
 * @author Vyacheslav Rusakov
 * @since 09.03.2025
 */
@SuppressFBWarnings({"CT_CONSTRUCTOR_THROW", "EQ_DOESNT_OVERRIDE_EQUALS", "SE_BAD_FIELD"})
public class DropwizardBundlesTracker extends ArrayList<ConfiguredBundle> {
    private final Logger logger = LoggerFactory.getLogger(DropwizardBundlesTracker.class);

    private final StatsInfo stats;
    private final StartupTimeInfo info;
    // OverallTime stat would finish with lifecycle listener event slightly earlier than listener in startup
    // diagnostic (because it will be registered later). Use custom timer for more accurate time measuring
    private final Stopwatch webTimer = Stopwatch.createUnstarted();

    // start timer since guice bundle startup - the earliest point we can track
    public DropwizardBundlesTracker(final StatsInfo stats, final StartupTimeInfo info, final Bootstrap bootstrap) {
        this.stats = stats;
        this.info = info;
        injectTracker(bootstrap);
    }

    public Stopwatch getWebTimer() {
        return webTimer;
    }

    @Override
    public boolean add(final ConfiguredBundle configuredBundle) {
        // intercept bundle addition - at this moment bundle initialization already done, so we will eventually know
        // bundles initialization done point
        // Wrap with tracker to know the point of bundles run done (there may be other bundles registered after guice)
        super.add(new BundleRunTracker(configuredBundle));
        // register time since initialization start! Because bundles can init other bundles we can't know exact
        // bundle init time, but can show time point when init was done!
        info.getInitPoints().put(configuredBundle.getClass().getSimpleName(), stats.duration(Stat.OverallTime));
        // last init done - init phase completed
        info.setInitTime(stats.duration(Stat.OverallTime));
        info.setInitInstallersTime(stats.duration(Stat.InstallersTime));
        info.setInitExtensionsTime(stats.duration(Stat.ExtensionsRecognitionTime));
        info.setInitListenersTime(stats.duration(Stat.ListenersTime));
        return true;
    }

    @Override
    public void add(final int index, final ConfiguredBundle element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(final Collection<? extends ConfiguredBundle> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(final int index, final Collection<? extends ConfiguredBundle> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ConfiguredBundle set(final int index, final ConfiguredBundle element) {
        throw new UnsupportedOperationException();
    }

    private void injectTracker(final Bootstrap bootstrap) {
        try {
            final Field configuredBundles = Bootstrap.class.getDeclaredField("configuredBundles");
            configuredBundles.setAccessible(true);
            final List<ConfiguredBundle> existing = (List<ConfiguredBundle>) configuredBundles.get(bootstrap);
            if (!existing.isEmpty()) {
                logger.warn("Initialization time not tracked for bundles (move them after guice bundle to "
                        + "measure time): {}", existing.stream()
                        .map(configuredBundle -> configuredBundle.getClass().getSimpleName())
                        .collect(Collectors.joining(", ")));
                // pack with tracker (for run phase)
                existing.forEach(this::add);
            }
            configuredBundles.set(bootstrap, this);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to inject bootstrap bundles tracker", e);
        }
    }

    /**
     * Wrapper object (delegate) for registered dropwizard bundles to be able to measure bundle run time.
     */
    private class BundleRunTracker implements ConfiguredBundle<Configuration> {

        private final ConfiguredBundle bundle;

        BundleRunTracker(final ConfiguredBundle bundle) {
            this.bundle = bundle;
        }

        @Override
        public void run(final Configuration configuration, final Environment environment) throws Exception {
            if (info.getDwPreRunTime() == null) {
                // time between last dw bundle init and first run (config and environment creation)
                info.setDwPreRunTime(stats.duration(Stat.OverallTime).minus(info.getInitTime()));
            }
            final Stopwatch bundleTimer = Stopwatch.createStarted();
            bundle.run(configuration, environment);
            info.getRunTimes().put(bundle.getClass().getSimpleName(), bundleTimer.stop().elapsed());
            // last bundle run end - end of run phase (application run can't be tracked)
            info.setRunPoint(stats.duration(Stat.OverallTime));
            info.setRunListenersTime(stats.duration(Stat.ListenersTime).minus(info.getInitListenersTime()));
            // reset because it would start after each dropwizard bundle run
            webTimer.reset().start();
        }
    }
}
