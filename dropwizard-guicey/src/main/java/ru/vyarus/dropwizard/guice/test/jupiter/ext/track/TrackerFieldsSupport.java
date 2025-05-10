package ru.vyarus.dropwizard.guice.test.jupiter.ext.track;

import com.google.common.base.Preconditions;
import com.google.inject.Binding;
import org.junit.jupiter.api.extension.ExtensionContext;
import ru.vyarus.dropwizard.guice.debug.util.RenderUtils;
import ru.vyarus.dropwizard.guice.test.jupiter.env.TestExtension;
import ru.vyarus.dropwizard.guice.test.jupiter.env.field.AnnotatedField;
import ru.vyarus.dropwizard.guice.test.jupiter.env.field.AnnotatedTestFieldSetup;
import ru.vyarus.dropwizard.guice.test.jupiter.env.listen.EventContext;
import ru.vyarus.dropwizard.guice.test.track.Tracker;
import ru.vyarus.dropwizard.guice.test.track.TrackersHook;
import ru.vyarus.dropwizard.guice.test.track.stat.TrackerStats;
import ru.vyarus.dropwizard.guice.test.util.PrintUtils;
import ru.vyarus.dropwizard.guice.test.util.TestSetupUtils;

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
public class TrackerFieldsSupport extends AnnotatedTestFieldSetup<TrackBean, Tracker> {
    // test context storage key for resolved fields
    private static final String TEST_TRACKER_FIELDS = "TEST_TRACKER_FIELDS";
    private static final String FIELD_TRACKER = "FIELD_TRACKER";
    private static final String DOUBLE_NL = ":\n\n";

    private final TrackersHook hook = new TrackersHook();

    public TrackerFieldsSupport() {
        super(TrackBean.class, Tracker.class, TEST_TRACKER_FIELDS);
    }

    @Override
    protected void fieldDetected(final ExtensionContext context,
                                 final AnnotatedField<TrackBean, Tracker> field) {
        final Class<?> type = field.getTypeParameters().get(0);
        if (type == Object.class) {
            throw new IllegalStateException(getDeclarationErrorPrefix(field) + "tracked service must be declared as "
                    + "a tracker object generic: Tracker<Bean>");
        }
    }

    @Override
    protected void registerHooks(final TestExtension extension) {
        extension.hooks(hook);
    }

    @Override
    protected <K> void initializeField(final AnnotatedField<TrackBean, Tracker> field, final Tracker userValue) {
        Preconditions.checkState(userValue == null, getDeclarationErrorPrefix(field)
                + "tracker instance can't be provided manually");
        final Class<?> type = field.getTypeParameters().get(0);
        final TrackBean ann = field.getAnnotation();
        final Tracker<?> tracker = hook.track(type)
                .trace(ann.trace())
                .slowMethods(ann.slowMethods(), ann.slowMethodsUnit())
                .keepRawObjects(ann.keepRawObjects())
                .maxStringLength(ann.maxStringLength())
                .add();
        field.setCustomData(FIELD_TRACKER, tracker);

    }

    @Override
    protected void beforeValueInjection(final EventContext context, final AnnotatedField<TrackBean, Tracker> field) {
        final Tracker<?> tracker = Preconditions.checkNotNull(field.getCustomData(FIELD_TRACKER));
        final Binding<?> binding = context.getInjector().getBinding(tracker.getType());
        Preconditions.checkState(!isInstanceBinding(binding), getDeclarationErrorPrefix(field)
                + "target bean '%s' bound by instance and so can't be tracked", tracker.getType().getSimpleName());
    }

    @Override
    protected Tracker injectFieldValue(final EventContext context, final AnnotatedField<TrackBean, Tracker> field) {
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
}
