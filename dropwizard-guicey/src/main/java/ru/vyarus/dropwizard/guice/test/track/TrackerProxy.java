package ru.vyarus.dropwizard.guice.test.track;

import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Stopwatch;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import ru.vyarus.dropwizard.guice.test.util.PrintUtils;

/**
 * AOP interceptor redirect calls from the real bean into spy object, which was created around the same real bean.
 * <p>
 * There is a chicken-egg problem: service binding can't be overridden (with spy instance), because spy requires
 * service instance for construction. So, instead of replacing bean, we intercept bean calls. Actual spy object
 * is created lazily just after injector creation. On the first call, AOP interceptor breaks the current aop chain
 * (if other interceptors registered) and redirect calls to spy, which again calls the same service (including
 * aop handler), but, this time, it processes normally.
 *
 * @param <T> bean type
 */
public class TrackerProxy<T> implements MethodInterceptor {
    private final Tracker<T> tracker;
    private final int maxToString;

    /**
     * Create proxy.
     *
     * @param type    service type
     * @param config  config
     * @param metrics metrics
     */
    public TrackerProxy(final Class<T> type, final TrackerConfig config, final MetricRegistry metrics) {
        this.maxToString = config.getMaxStringLength();
        this.tracker = new Tracker<>(type, config, metrics);
    }

    /**
     * @return tracker instance
     */
    public Tracker<T> getTracker() {
        return tracker;
    }

    @Override
    public synchronized Object invoke(final MethodInvocation methodInvocation) throws Throwable {
        final long start = System.currentTimeMillis();
        final Stopwatch timer = Stopwatch.createStarted();
        // objects might be changed after or during method execution
        final Object[] arguments = methodInvocation.getArguments();
        final String[] args = PrintUtils.toStringArguments(arguments, maxToString);
        final boolean[] stringMarkers = new boolean[args.length + 1];
        for (int i = 0; i < args.length; i++) {
            stringMarkers[i] = arguments[i] instanceof String;
        }
        Object result = null;
        Throwable error = null;
        try {
            result = methodInvocation.proceed();
            return result;
        } catch (Throwable e) {
            error = e;
            throw e;
        } finally {
            stringMarkers[stringMarkers.length - 1] = result instanceof String;
            tracker.add(methodInvocation.getMethod(),
                    PrintUtils.identity(methodInvocation.getThis()),
                    start,
                    timer.elapsed(),
                    arguments,
                    args,
                    result,
                    error != null ? null : PrintUtils.toStringValue(result, maxToString),
                    error,
                    stringMarkers);
        }
    }
}
