package ru.vyarus.dropwizard.guice.test.jupiter.ext.conf.track;

import org.junit.jupiter.api.extension.ExtensionContext;
import ru.vyarus.dropwizard.guice.test.util.PrintUtils;
import ru.vyarus.dropwizard.guice.test.util.TestSetupUtils;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Guicey test extensions debug reports builder.
 *
 * @author Vyacheslav Rusakov
 * @since 07.03.2025
 */
public final class TrackerReportBuilder {

    private TrackerReportBuilder() {
    }

    public static String buildSetupReport(final String configPrefix,
                                          final List<String> setups,
                                          final List<String> hooks) {
        // using config prefix to differentiate outputs for parallel execution
        final StringBuilder res = new StringBuilder(500).append("\nGuicey test extensions (")
                .append(configPrefix).append(".):\n\n");
        if (!setups.isEmpty()) {
            res.append("\tSetup objects = \n");
            logTracks(res, setups);
        }

        if (!hooks.isEmpty()) {
            res.append("\tTest hooks = \n");
            logTracks(res, hooks);
        }
        return res.toString();
    }

    public static String buildPerformanceReport(final List<PerformanceTrack> tracks,
                                                final ExtensionContext context,
                                                final GuiceyTestTime phase) {
        final StringBuilder res = new StringBuilder();
        Duration overall = Duration.ZERO;
        Duration increase = Duration.ZERO;
        for (PerformanceTrack root : tracks) {
            if (!root.isRoot()) {
                continue;
            }
            overall = overall.plus(root.getOverall());
            if (root.isDurationChanged()) {
                increase = increase.plus(root.getIncrease());
                res.append("\n\t").append(root).append('\n');

                for (PerformanceTrack track : tracks) {
                    if (track.isRoot() || track.phase != root.name) {
                        continue;
                    }
                    if (root.isDurationChanged()) {
                        res.append("\t\t").append(track).append('\n');
                    }
                }
            }
        }

        // merge increase delta to start tracking new increases
        tracks.forEach(PerformanceTrack::markLogged);

        final Duration lastOverall = overall.minus(increase);

        final String title = PrintUtils.getPerformanceReportSeparator(context)
                + "Guicey time after [" + phase.getDisplayName() + "] of "
                + TestSetupUtils.getContextTestName(context)
                + ": " + PrintUtils.renderTime(overall,
                lastOverall.equals(Duration.ZERO) ? null : increase);

        return title + "\n" + res;
    }

    public static String buildConfigsReport(final String configPrefix, final List<String> modifiers) {
        final StringBuilder res = new StringBuilder(100);
        for (Map.Entry<Object, Object> entry : System.getProperties().entrySet()) {
            final String key = (String) entry.getKey();
            if (key.startsWith(configPrefix)) {
                res.append(String.format("\t %20s = %s%n",
                        key.substring(configPrefix.length() + 1), entry.getValue()));
            }
        }

        final boolean hasOverrides = !res.isEmpty();

        if (!modifiers.isEmpty()) {
            res.append("\nConfiguration modifiers:\n");
            logTracks(res, modifiers);
        }

        return (hasOverrides ? "\nConfiguration overrides (" + configPrefix + ".):\n" : "") + res;
    }

    private static void logTracks(final StringBuilder res, final List<String> tracks) {
        for (String st : tracks) {
            res.append("\t\t").append(st).append('\n');
        }
        res.append('\n');
    }
}
