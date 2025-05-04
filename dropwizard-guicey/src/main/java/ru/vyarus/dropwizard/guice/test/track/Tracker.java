package ru.vyarus.dropwizard.guice.test.track;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.mockito.Mockito;
import org.mockito.internal.creation.DelegatingMethod;
import org.mockito.internal.debugging.LocationFactory;
import org.mockito.internal.invocation.InterceptedInvocation;
import org.mockito.internal.invocation.mockref.MockStrongReference;
import org.mockito.internal.progress.SequenceNumber;
import org.mockito.internal.stubbing.InvocationContainerImpl;
import org.mockito.internal.util.MockUtil;
import org.mockito.invocation.MatchableInvocation;
import org.mockito.stubbing.OngoingStubbing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vyarus.dropwizard.guice.test.track.stat.TrackerStats;
import ru.vyarus.dropwizard.guice.test.util.PrintUtils;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Tracker object used for bean calls registration (together with
 * {@link ru.vyarus.dropwizard.guice.test.jupiter.ext.track.TrackBean} or
 * {@link ru.vyarus.dropwizard.guice.test.track.TrackersHook}).
 * <p>
 * Use metrics timer to aggregate method calls statistics. Use {@link #getStats()} for report building.
 * <p>
 * Tracked methods filtering implemented with mockito: {@link #findTracks(java.util.function.Function)}.
 * <p>
 * By default, recorded tracks cleared after each test.
 *
 * @param <T> bean type
 * @author Vyacheslav Rusakov
 * @since 11.02.2025
 */
@SuppressWarnings("ClassDataAbstractionCoupling")
public class Tracker<T> {
    private final Logger logger = LoggerFactory.getLogger(Tracker.class);

    private final Class<T> type;
    private final TrackerConfig config;
    private final Duration warn;
    private final List<MethodTrack> tracks = new CopyOnWriteArrayList<>();
    private final Map<Method, Timer> timers = new ConcurrentHashMap<>();

    private MetricRegistry metrics;
    private Object innerMock;
    private MockStrongReference<Object> reference;

    public Tracker(final Class<T> type,
                   final TrackerConfig config,
                   final MetricRegistry metrics) {
        this.type = type;
        this.config = config;
        this.warn = config.getSlowMethods() > 0
                ? Duration.of(config.getSlowMethods(), config.getSlowMethodsUnit()) : null;
        this.metrics = metrics;
    }

    /**
     * @return type of tracked bean
     */
    public Class<T> getType() {
        return type;
    }

    /**
     * @return true if no method calls registered, false otherwise
     */
    public boolean isEmpty() {
        return tracks.isEmpty();
    }

    /**
     * @return count of tracked methods
     */
    public int size() {
        return tracks.size();
    }

    /**
     * Tracks sorted by start time.
     *
     * @return all tracked method calls
     */
    public List<MethodTrack> getTracks() {
        return new ArrayList<>(tracks);
    }

    /**
     * @return last tracked method call
     * @throws java.lang.IllegalStateException if no calls tracked (error thrown to simplify usage - no additional
     *                                         checks required in test)
     */
    public MethodTrack getLastTrack() {
        Preconditions.checkState(!tracks.isEmpty(), "No tracks registered");
        return tracks.get(tracks.size() - 1);
    }

    /**
     * Returns last (count) tracks in execution order.
     *
     * @param count last tracks count
     * @return last tracks (count)
     * @throws java.lang.IllegalStateException if there is not enough recorded tracks
     */
    public List<MethodTrack> getLastTracks(final int count) {
        Preconditions.checkState(tracks.size() >= count,
                "Not enough tracks registered: requested %s but only %s registered", count, tracks.size());
        return tracks.subList(tracks.size() - count, tracks.size());
    }

    /**
     * NOTE: This is just an example usage - you can create a stats object with filtered methods
     * (or with methods from multiple trackers).
     *
     * @return stats for all tracked methods (for reporting)
     */
    public TrackerStats getStats() {
        return new TrackerStats(tracks);
    }

    /**
     * Tracked methods filtering using mockito. Useful because mockito provides type-safe syntax. For example,
     * search by method: {@code find(mock -> when(mock.something())}. Search by method with exact argument == 1:
     * {@code find(mock -> when(mock.something(intThat(argument -> argument == 1)))}.
     *
     * @param where search condition definition
     * @return list of matched tracks
     */
    // not just find for groovy support (find is a default groovy method)
    public List<MethodTrack> findTracks(final Function<T, OngoingStubbing<?>> where) {
        final OngoingStubbing<?> apply = where.apply(mock());
        final InvocationContainerImpl invocationContainer = MockUtil.getInvocationContainer(mock());
        try {
            // it is a hack, but it's the only way to access matcher
            final MatchableInvocation matcher = (MatchableInvocation) FieldUtils
                    .readDeclaredField(invocationContainer, "invocationForStubbing", true);

            final List<MethodTrack> result = new ArrayList<>();
            for (final MethodTrack track : tracks) {
                if (matcher.matches(from(track))) {
                    result.add(track);
                }
            }
            return result;

        } catch (Exception ex) {
            throw new IllegalStateException("Failed to find tracker method", ex);
        } finally {
            // to finish stubbing config and avoid errors
            apply.thenReturn(null);
            MockUtil.resetMock(mock());
        }
    }

    /**
     * Cleanup recorded tracks. By default, called automatically after each test method.
     */
    public void clear() {
        tracks.clear();
        timers.clear();
        metrics = new MetricRegistry();
    }

    @SuppressWarnings({"ParameterNumber", "PMD.ExcessiveParameterList", "PMD.UseVarargs", "PMD.SystemPrintln"})
    void add(final Method method,
             final String instanceHash,
             final long started,
             final Duration duration,
             final Object[] rawArguments,
             final String[] arguments,
             final Object rawResult,
             final String result,
             final Throwable throwable,
             final boolean[] stringMarkers) {
        final Timer timer = getTimer(method);
        timer.update(duration);
        final MethodTrack track = new MethodTrack(type, method, instanceHash, started, duration,
                config.isKeepRawObjects() ? rawArguments : null,
                arguments,
                config.isKeepRawObjects() ? rawResult : null,
                result, throwable, stringMarkers, timer);
        synchronized (tracks) {
            tracks.add(track);
            // sort to order tracks according to START TIME and not by the end time, as they add here
            // (important for getLastTracks feature)
            Collections.sort(tracks);
        }
        if (config.isTrace() || (warn != null && duration.compareTo(warn) > 0)) {
            final String msg = "\\\\\\---[Tracker<" + type.getSimpleName() + ">]"
                    + String.format(" %-12s <@%s> .%s",
                    PrintUtils.ms(track.getDuration()), instanceHash, track.toStringTrack());
            if (config.isTrace()) {
                // logger is not used here because debug and trace would not be visible and warnings would confuse
                System.out.println(msg);
            } else {
                logger.warn("\n" + msg);
            }
        }
    }

    private Timer getTimer(final Method method) {
        return timers.computeIfAbsent(method,
                k -> metrics.timer(type.getName() + "." + method.getName() + "."
                        + Arrays.stream(method.getParameterTypes())
                        .map(Class::getSimpleName)
                        .collect(Collectors.joining(","))));
    }

    /**
     * @return mock to use for methods search ({@link #findTracks(java.util.function.Function)})
     */
    @SuppressWarnings("unchecked")
    private T mock() {
        // lazy mock creation (if used)
        if (innerMock == null) {
            innerMock = Mockito.mock(type);
            reference = new MockStrongReference<>(innerMock, false);
        }
        return (T) innerMock;
    }

    private InterceptedInvocation from(final MethodTrack track) {
        return new InterceptedInvocation(
                reference,
                new DelegatingMethod(track.getMethod()),
                track.getRawArguments(),
                null,
                LocationFactory.create(),
                SequenceNumber.next());
    }
}
