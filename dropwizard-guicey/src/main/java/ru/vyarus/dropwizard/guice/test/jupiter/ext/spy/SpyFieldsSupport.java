package ru.vyarus.dropwizard.guice.test.jupiter.ext.spy;

import com.google.common.base.Preconditions;
import com.google.inject.Binding;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.mockito.Mockito;
import ru.vyarus.dropwizard.guice.debug.util.RenderUtils;
import ru.vyarus.dropwizard.guice.module.installer.util.InstanceUtils;
import ru.vyarus.dropwizard.guice.test.jupiter.env.TestExtension;
import ru.vyarus.dropwizard.guice.test.jupiter.env.field.AnnotatedField;
import ru.vyarus.dropwizard.guice.test.jupiter.env.field.AnnotatedTestFieldSetup;
import ru.vyarus.dropwizard.guice.test.jupiter.env.listen.EventContext;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.mock.MockBean;
import ru.vyarus.dropwizard.guice.test.spy.SpiesHook;
import ru.vyarus.dropwizard.guice.test.spy.SpyProxy;
import ru.vyarus.dropwizard.guice.test.util.PrintUtils;
import ru.vyarus.dropwizard.guice.test.util.TestSetupUtils;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * {@link ru.vyarus.dropwizard.guice.test.jupiter.ext.spy.SpyBean} test fields support implementation.
 * <p>
 * Annotated fields resolved in time of guicey extension initialization (beforeAll or beforeEach).
 * Register aop interceptor around target service to intercept all calls, and redirect all calls through spy object.
 * This way, real bean becomes spied and still injected everywhere.
 * <p>
 * Manual values are not supported: @MockBean should be used instead.
 * <p>
 * In beforeAll injects static values, in beforeEach inject both (in case if beforeAll wasn't called).
 * Calls spies reset after each test.
 *
 * @author Vyacheslav Rusakov
 * @since 10.02.2025
 */
public class SpyFieldsSupport extends AnnotatedTestFieldSetup<SpyBean, Object> {

    private static final String TEST_SPY_FIELDS = "TEST_SPY_FIELDS";
    private static final String FIELD_SPY = "FIELD_SPY";

    private final SpiesHook hook = new SpiesHook();

    /**
     * Create support.
     */
    public SpyFieldsSupport() {
        super(SpyBean.class, Object.class, TEST_SPY_FIELDS);
    }

    @Override
    protected void fieldDetected(final ExtensionContext context, final AnnotatedField<SpyBean, Object> field) {
        // nothing
    }

    @Override
    protected void registerHooks(final TestExtension extension) {
        extension.hooks(hook);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <K> void initializeField(final AnnotatedField<SpyBean, Object> field, final Object userValue) {
        if (userValue != null) {
            throw new IllegalStateException(getDeclarationErrorPrefix(field)
                    + "manual spy declaration is not supported. "
                    + "Use @" + MockBean.class.getSimpleName() + " instead to specify manual spy object.");
        }
        final SpyProxy<K> proxy = hook.spy((Class<K>) field.getType());
        final Class<Consumer<K>>[] initializers = (Class<Consumer<K>>[]) (Class[]) field.getAnnotation().initializers();
        for (Class<Consumer<K>> initializer : initializers) {
            proxy.withInitializer(InstanceUtils.create(initializer));
        }
        field.setCustomData(FIELD_SPY, proxy);
    }

    @Override
    protected void beforeValueInjection(final EventContext context, final AnnotatedField<SpyBean, Object> field) {
        final SpyProxy<?> spy = Preconditions.checkNotNull(field.getCustomData(FIELD_SPY));
        final Binding<?> binding = context.getInjector().getBinding(spy.getType());
        Preconditions.checkState(!isInstanceBinding(binding), getDeclarationErrorPrefix(field)
                + "target bean '%s' bound by instance and so can't be spied", spy.getType().getSimpleName());
    }

    @Override
    protected Object injectFieldValue(final EventContext context, final AnnotatedField<SpyBean, Object> field) {
        // inject already initialized spy from aop interceptor
        final SpyProxy aop = field.getCustomData(FIELD_SPY);
        return aop.getSpy();
    }

    @Override
    @SuppressWarnings("PMD.SystemPrintln")
    protected void report(final EventContext context, final List<AnnotatedField<SpyBean, Object>> annotatedFields) {
        final StringBuilder report = new StringBuilder("\nApplied spies (@")
                .append(SpyBean.class.getSimpleName()).append(") on ").append(setupContextName).append(":\n\n");
        fields.forEach(field -> report.append(
                String.format("\t%-30s %-20s%n",
                        '#' + field.getField().getName(),
                        RenderUtils.renderClassLine(field.getType()))));
        System.out.println(report);
    }

    @Override
    protected void beforeTest(final EventContext context,
                              final AnnotatedField<SpyBean, Object> field, final Object value) {
        // only after test (spy might be used in setup)
    }

    @Override
    @SuppressWarnings("PMD.SystemPrintln")
    protected void afterTest(final EventContext context,
                             final AnnotatedField<SpyBean, Object> field, final Object value) {
        if (field.getAnnotation().printSummary()) {
            final String res = Mockito.mockingDetails(value).printInvocations();
            System.out.println(PrintUtils.getPerformanceReportSeparator(context.getJunitContext())
                    + "@" + SpyBean.class.getSimpleName() + " stats on [After each] for "
                    + TestSetupUtils.getContextTestName(context.getJunitContext()) + ":\n\n"
                    + Arrays.stream(res.split("\n")).map(s -> "\t" + s).collect(Collectors.joining("\n")));
        }
        if (field.getAnnotation().autoReset() && value != null) {
            Mockito.reset(value);
        }
    }
}
