package ru.vyarus.dropwizard.guice.test.jupiter.ext.track.stat;

import ru.vyarus.dropwizard.guice.test.jupiter.ext.track.MethodTrack;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.track.Tracker;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Default tracker report implementation: shows all tracked methods, sorted by duration (the slowest go first).
 *
 * @author Vyacheslav Rusakov
 * @since 12.02.2025
 */
public class TrackerStats {

    private final List<MethodSummary> methods;

    @SuppressWarnings("unchecked")
    public TrackerStats(final Tracker... trackers) {
        this((List<MethodTrack>) Arrays.stream(trackers)
                .map(Tracker::getTracks)
                .flatMap(List::stream)
                .collect(Collectors.<MethodTrack>toList()));
    }

    public TrackerStats(final List<MethodTrack> tracks) {
        methods = buildSummary(tracks);
    }

    /**
     * @return methods summary sorted by the slowness (the slowest go first)
     */
    public List<MethodSummary> getMethods() {
        return methods;
    }

    /**
     * @return rendered report or null if no tracks
     */
    public String render() {
        if (methods.isEmpty()) {
            return null;
        }
        final String format = "%-40s %-50s %-10s %-10s %-10s %-10s %-10s %-10s %-10s%n";
        final StringBuilder builder = new StringBuilder();
        builder.append('\t').append(String.format(format,
                "[service]", "[method]", "[calls]", "[fails]", "[min]", "[max]", "[median]", "[75%]", "[95%]"));
        for (final MethodSummary summary : methods) {
            builder.append('\t').append(String.format(format,
                    summary.getService().getSimpleName(),
                    summary.toStringMethod(),
                    summary.getTracks()
                            + (summary.getInstancesCount() > 1 ? " (" + summary.getInstancesCount() + ")" : ""),
                    summary.getErrors(),
                    summary.getMin(),
                    summary.getMax(),
                    summary.getMedian(),
                    summary.get75thPercentile(),
                    summary.get95thPercentile()));
        }
        return builder.toString();
    }

    private List<MethodSummary> buildSummary(final List<MethodTrack> tracks) {
        final Map<Method, MethodSummary> idx = new HashMap<>();
        for (final MethodTrack track : tracks) {
            idx.computeIfAbsent(track.getMethod(),
                            method -> new MethodSummary(track.getService(), track.getMethod(), track.getTimer()))
                    .add(track);
        }
        final List<MethodSummary> res = new ArrayList<>(idx.values());
        Collections.sort(res);
        // top slow above
        Collections.reverse(res);
        return res;
    }
}
