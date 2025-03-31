package ru.vyarus.dropwizard.guice.test.jupiter.ext.track;

import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.inject.Binder;
import com.google.inject.Binding;
import com.google.inject.matcher.Matchers;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.junit.jupiter.api.extension.ExtensionContext;
import ru.vyarus.dropwizard.guice.debug.util.RenderUtils;
import ru.vyarus.dropwizard.guice.test.jupiter.env.field.AnnotatedField;
import ru.vyarus.dropwizard.guice.test.jupiter.env.field.AnnotatedTestFieldSetup;
import ru.vyarus.dropwizard.guice.test.jupiter.env.listen.EventContext;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.track.stat.TrackerStats;
import ru.vyarus.dropwizard.guice.test.util.PrintUtils;
import ru.vyarus.dropwizard.guice.test.util.TestSetupUtils;

import java.time.Duration;
import java.util.List;

/**
 * {@link ru.vyarus.dropwizard.guice.test.jupiter.ext.track.TrackBean} test fields support implementation.
 * <p>
 * Annotated fields resolved in time of guicey extension initialization (beforeAll or beforeEach).
 * Register aop interceptor around target service to intercept all calls.
 * <p>
 * In beforeAll injects static values, in beforeEach inject both (in case if beforeAll wasn't called).
 * Calls tracker reset after each test.
 *
 * @author Vyacheslav Rusakov
 * @since 11.02.2025
 */
public class TrackersSupport extends AnnotatedTestFieldSetup<TrackBean, Tracker> {
    // test context storage key for resolved fields
    private static final String TEST_TRACKER_FIELDS = "TEST_TRACKER_FIELDS";
    private static final String FIELD_TRACKER = "FIELD_TRACKER";
    private static final String DOUBLE_NL = ":\n\n";

    // independent from dropwizard app registry
    private final MetricRegistry metrics = new MetricRegistry();

    public TrackersSupport() {
        super(TrackBean.class, Tracker.class, TEST_TRACKER_FIELDS);
    }

    @Override
    protected void validateDeclaration(final ExtensionContext context,
                                       final AnnotatedField<TrackBean, Tracker> field) {
        final Class<?> type = field.getTypeParameters().get(0);
        if (type == Object.class) {
            throw new IllegalStateException(getDeclarationErrorPrefix(field) + "tracked service must be declared as "
                    + "a tracker object generic: Tracker<Bean>");
        }
    }

    @Override
    protected <K> void bindFieldValue(final Binder binder,
                                      final AnnotatedField<TrackBean, Tracker> field, final Tracker value) {
        throw new IllegalStateException(getDeclarationErrorPrefix(field)
                + "tracker instance can't be provided manually");
    }

    @Override
    protected <K> void bindField(final Binder binder, final AnnotatedField<TrackBean, Tracker> field) {
        final Class<?> type = field.getTypeParameters().get(0);
        final TrackedBean aop = new TrackedBean(type, field.getAnnotation(), metrics);
        field.setCustomData(FIELD_TRACKER, aop.getTracker());
        // real binding isn't overridden, just used aop to intercept all calls
        binder.bindInterceptor(Matchers.only(type), Matchers.any(), aop);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void validateBinding(final EventContext context, final AnnotatedField<TrackBean, Tracker> field) {
        final Tracker tracker = Preconditions.checkNotNull(field.getCustomData(FIELD_TRACKER));
        final Binding binding = context.getInjector().getBinding(tracker.getType());
        Preconditions.checkState(!isInstanceBinding(binding), getDeclarationErrorPrefix(field)
                + "target bean '%s' bound by instance and so can't be tracked", tracker.getType().getSimpleName());
    }

    @Override
    protected Tracker getFieldValue(final EventContext context, final AnnotatedField<TrackBean, Tracker> field) {
        return field.getCustomData(FIELD_TRACKER);
    }

    @Override
    @SuppressWarnings("PMD.SystemPrintln")
    protected void report(final EventContext context,
                          final List<AnnotatedField<TrackBean, Tracker>> annotatedFields) {
        final StringBuilder report = new StringBuilder("\nApplied trackers (@")
                .append(TrackBean.class.getSimpleName()).append(") on ").append(setupContextName).append(DOUBLE_NL);
        fields.forEach(field -> report.append(
                String.format("\t%-30s %-20s%n",
                        '#' + field.getField().getName(),
                        RenderUtils.renderClassLine(field.<Tracker>getCustomData(FIELD_TRACKER).getType()))));
        System.out.println(report);
    }

    @Override
    protected void beforeTest(final EventContext context,
                              final AnnotatedField<TrackBean, Tracker> field, final Tracker value) {
        // no clean to keep tracks from setup stage
    }

    @Override
    @SuppressWarnings("PMD.SystemPrintln")
    protected void afterTest(final EventContext context,
                             final AnnotatedField<TrackBean, Tracker> field, final Tracker value) {
        final TrackBean ann = field.getAnnotation();
        if (ann.printSummary()) {
            final Tracker tracker = field.getCustomData(FIELD_TRACKER);
            // report for exact tracker (activated with the annotation option - works without debug enabling)
            if (!tracker.isEmpty()) {
                System.out.println(PrintUtils.getPerformanceReportSeparator(context.getJunitContext())
                        + "Tracker<" + tracker.getType().getSimpleName() + ">" + " stats (sorted by median) for "
                        + TestSetupUtils.getContextTestName(context.getJunitContext()) + DOUBLE_NL
                        + tracker.getStats().render());
            }
        }
        if (ann.autoReset()) {
            value.clear();
        }
    }

    @Override
    @SuppressWarnings("PMD.SystemPrintln")
    public void afterEach(final EventContext context) {
        if (context.isDebug()) {
            final Tracker[] trackers = fields.stream()
                    .map(field -> (Tracker) field.getCustomData(FIELD_TRACKER))
                    .toArray(Tracker[]::new);
            if (trackers.length > 0) {
                // report all trackers - works only with debug
                System.out.println(PrintUtils.getPerformanceReportSeparator(context.getJunitContext())
                        + "Trackers stats (sorted by median) for "
                        + TestSetupUtils.getContextTestName(context.getJunitContext()) + DOUBLE_NL
                        + new TrackerStats(trackers).render());
            }
        }
        // cleanup all tracks
        super.afterEach(context);
    }

    /**
     * AOP interceptor redirect calls from the real bean into spy object, which was created around the same real bean.
     * <p>
     * There is a chicken-egg problem: service binding can't be overridden (with spy instance), because spy requires
     * service instance for construction. So, instead of replacing bean, we intercept bean calls. Actual spy object
     * is created lazily just after injector creation. On the first call, AOP interceptor breaks the current aop chain
     * (if other interceptors registered) and redirect calls to spy, which again calls the same service (including
     * aop handler), but, this time, it processes normally.
     */
    public static class TrackedBean implements MethodInterceptor {
        private final Class<?> type;
        private final Tracker<?> tracker;
        private final int maxToString;

        public TrackedBean(final Class<?> type, final TrackBean ann, final MetricRegistry metrics) {
            this.type = type;
            this.maxToString = ann.maxStringLength();
            this.tracker = new Tracker<>(type, ann.trace(), ann.keepRawObjects(),
                    ann.slowMethods() > 0 ? Duration.of(ann.slowMethods(), ann.slowMethodsUnit()) : null, metrics);
        }

        public Object getTracker() {
            return tracker;
        }

        @Override
        @SuppressWarnings("PMD.AvoidSynchronizedAtMethodLevel")
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
}
