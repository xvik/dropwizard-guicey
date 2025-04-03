package ru.vyarus.dropwizard.guice.test.jupiter.ext.mock;

import com.google.common.base.Preconditions;
import com.google.inject.Binder;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.mockito.Mockito;
import org.mockito.internal.util.MockUtil;
import ru.vyarus.dropwizard.guice.debug.util.RenderUtils;
import ru.vyarus.dropwizard.guice.test.jupiter.env.field.AnnotatedField;
import ru.vyarus.dropwizard.guice.test.jupiter.env.field.AnnotatedTestFieldSetup;
import ru.vyarus.dropwizard.guice.test.jupiter.env.listen.EventContext;
import ru.vyarus.dropwizard.guice.test.util.PrintUtils;
import ru.vyarus.dropwizard.guice.test.util.TestSetupUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * {@link ru.vyarus.dropwizard.guice.test.jupiter.ext.mock.MockBean} test fields support implementation.
 * <p>
 * Annotated fields resolved in time of guicey extension initialization (beforeAll or beforeEach).
 * Register override bindings for provided mock instances (manual or created automatically).
 * See debug report to be sure what value was actually used: manual or automatic (field might be assigned too late).
 * <p>
 * In beforeAll injects static values, in beforeEach inject both (in case if beforeAll wasn't called).
 * Calls mocks reset after each test.
 *
 * @author Vyacheslav Rusakov
 * @since 10.02.2025
 */
public class MocksSupport extends AnnotatedTestFieldSetup<MockBean, Object> {

    private static final String TEST_MOCK_FIELDS = "TEST_MOCK_FIELDS";
    private static final String FIELD_MOCK = "FIELD_MOCK";

    public MocksSupport() {
        super(MockBean.class, Object.class, TEST_MOCK_FIELDS);
    }

    @Override
    protected void validateDeclaration(final ExtensionContext context,
                                       final AnnotatedField<MockBean, Object> field) {
        // nothing
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <K> void bindFieldValue(final Binder binder,
                                      final AnnotatedField<MockBean, Object> field, final Object value) {
        Preconditions.checkState(MockUtil.isMock(value), getDeclarationErrorPrefix(field)
                + "initialized instance is not a mockito mock object. Either provide correct mock or remove value "
                + "and let extension create mock automatically.");
        final Class<? super K> type = field.getType();
        // bind provided spy (the same effect as if it would be a StubBean)
        binder.bind(type).toInstance((K) value);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <K> void bindField(final Binder binder, final AnnotatedField<MockBean, Object> field) {
        final Class<? super K> type = field.getType();
        final Object mock = Mockito.mock(type);
        field.setCustomData(FIELD_MOCK, mock);
        binder.bind(type).toInstance((K) mock);
    }

    @Override
    protected void validateBinding(final EventContext context, final AnnotatedField<MockBean, Object> field) {
        // nothing: no existing binding validation because jit injections might be used
    }

    @Override
    protected Object getFieldValue(final EventContext context, final AnnotatedField<MockBean, Object> field) {
        return Preconditions.checkNotNull(field.getCustomData(FIELD_MOCK), "Mock not created");
    }

    @Override
    @SuppressWarnings("PMD.SystemPrintln")
    protected void report(final EventContext context,
                          final List<AnnotatedField<MockBean, Object>> annotatedFields) {
        final StringBuilder report = new StringBuilder("\nApplied mocks (@")
                .append(MockBean.class.getSimpleName()).append(") on ").append(setupContextName).append(":\n\n");
        fields.forEach(field -> report.append(
                String.format("\t%-30s %-20s %s%n",
                        '#' + field.getField().getName(),
                        RenderUtils.renderClassLine(field.getType()),
                        field.isCustomDataSet(FIELD_MANUAL) ? "MANUAL" : "AUTO")));
        System.out.println(report);
    }

    @Override
    protected void beforeTest(final EventContext context,
                              final AnnotatedField<MockBean, Object> field, final Object value) {
        // only after test (mock might be used in setup)
    }

    @Override
    @SuppressWarnings("PMD.SystemPrintln")
    protected void afterTest(final EventContext context,
                             final AnnotatedField<MockBean, Object> field, final Object value) {
        if (field.getAnnotation().printSummary()) {
            final String res = Mockito.mockingDetails(value).printInvocations();
            System.out.println(PrintUtils.getPerformanceReportSeparator(context.getJunitContext())
                    + "@" + MockBean.class.getSimpleName() + " stats on [After each] for "
                    + TestSetupUtils.getContextTestName(context.getJunitContext()) + ":\n\n"
                    + Arrays.stream(res.split("\n")).map(s -> "\t" + s).collect(Collectors.joining("\n")));
        }
        if (field.getAnnotation().autoReset()) {
            Mockito.reset(value);
        }
    }
}
