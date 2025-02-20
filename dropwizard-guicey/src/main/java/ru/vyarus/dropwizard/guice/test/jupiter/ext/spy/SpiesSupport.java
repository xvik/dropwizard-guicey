package ru.vyarus.dropwizard.guice.test.jupiter.ext.spy;

import com.google.common.base.Preconditions;
import com.google.inject.Binder;
import com.google.inject.Binding;
import com.google.inject.Provider;
import com.google.inject.matcher.Matchers;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.mockito.Mockito;
import ru.vyarus.dropwizard.guice.debug.util.RenderUtils;
import ru.vyarus.dropwizard.guice.test.jupiter.env.field.AnnotatedField;
import ru.vyarus.dropwizard.guice.test.jupiter.env.field.AnnotatedTestFieldSetup;
import ru.vyarus.dropwizard.guice.test.jupiter.env.listen.EventContext;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.mock.MockBean;
import ru.vyarus.dropwizard.guice.test.util.PrintUtils;
import ru.vyarus.dropwizard.guice.test.util.TestSetupUtils;

import java.util.Arrays;
import java.util.List;
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
public class SpiesSupport extends AnnotatedTestFieldSetup<SpyBean, Object> {

    private static final String TEST_SPY_FIELDS = "TEST_SPY_FIELDS";
    private static final String FIELD_SPY = "FIELD_SPY";

    public SpiesSupport() {
        super(SpyBean.class, Object.class, TEST_SPY_FIELDS);
    }

    @Override
    protected void validateDeclaration(final ExtensionContext context, final AnnotatedField<SpyBean, Object> field) {
        // nothing
    }

    @Override
    protected <K> void bindFieldValue(final Binder binder,
                                      final AnnotatedField<SpyBean, Object> field, final Object value) {
        throw new IllegalStateException(getDeclarationErrorPrefix(field) + "manual spy declaration is not supported. "
                + "Use @" + MockBean.class.getSimpleName() + " instead to specify manual spy object.");
    }

    @Override
    protected <K> void bindField(final Binder binder, final AnnotatedField<SpyBean, Object> field) {
        final Class<?> type = field.getType();
        final SpiedBean aop = new SpiedBean(type, binder.getProvider(type));
        field.setCustomData(FIELD_SPY, aop);
        // real binding isn't overridden, just used aop to intercept call and redirect into spy
        binder.bindInterceptor(Matchers.only(type), Matchers.any(), aop);
    }

    @Override
    protected void validateBinding(final EventContext context, final AnnotatedField<SpyBean, Object> field) {
        final SpiedBean spy = Preconditions.checkNotNull(field.getCustomData(FIELD_SPY));
        final Binding binding = context.getInjector().getBinding(spy.getType());
        Preconditions.checkState(!isInstanceBinding(binding), getDeclarationErrorPrefix(field)
                + "target bean '%s' bound by instance and so can't be spied", spy.getType().getSimpleName());
    }

    @Override
    protected Object getFieldValue(final EventContext context, final AnnotatedField<SpyBean, Object> field) {
        // inject already initialized spy from aop interceptor
        final SpiedBean aop = field.getCustomData(FIELD_SPY);
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
        if (field.getAnnotation().autoReset()) {
            Mockito.reset(value);
        }
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
    public static class SpiedBean implements MethodInterceptor {
        private final Class<?> type;
        private final Provider<?> instanceProvider;
        private volatile Object spy;

        public SpiedBean(final Class<?> type, final Provider<?> instanceProvider) {
            this.type = type;
            this.instanceProvider = instanceProvider;
        }

        public Class<?> getType() {
            return type;
        }

        @SuppressWarnings("PMD.AvoidSynchronizedAtMethodLevel")
        public synchronized Object getSpy() {
            if (spy == null) {
                // lazy spy init
                final Object bean = Preconditions.checkNotNull(instanceProvider.get());
                spy = Mockito.spy(bean);
            }
            return spy;
        }

        @Override
        @SuppressWarnings({"PMD.AvoidSynchronizedAtMethodLevel", "PMD.CompareObjectsWithEquals"})
        public synchronized Object invoke(final MethodInvocation methodInvocation) throws Throwable {
            // WARNING: for proper execution, this requires this AOP handler to be top most!
            // (otherwise, some interceptors would be called multiple times)

            final boolean isSpyCalled = methodInvocation.getThis() == spy;
            if (isSpyCalled) {
                // second call (from spy) - normal execution, including all underlying aop
                return methodInvocation.proceed();
            }

            // first call - interceptor breaks the AOP chain by calling the same method on spy object, which
            // wraps the same proxied bean (so interceptor would be called second time)
            return methodInvocation.getMethod().invoke(getSpy(), methodInvocation.getArguments());
        }
    }
}
