package ru.vyarus.dropwizard.guice.test.jupiter.ext.stub;

import com.google.inject.Binder;
import com.google.inject.Singleton;
import org.junit.jupiter.api.extension.ExtensionContext;
import ru.vyarus.dropwizard.guice.test.jupiter.env.field.AnnotatedField;
import ru.vyarus.dropwizard.guice.test.jupiter.env.field.AnnotatedTestFieldSetup;
import ru.vyarus.dropwizard.guice.test.jupiter.env.listen.EventContext;

import java.util.List;

/**
 * {@link ru.vyarus.dropwizard.guice.test.jupiter.ext.stub.StubBean} test fields support implementation.
 * <p>
 * Annotated fields resolved in time of guicey extension initialization (beforeAll or beforeEach).
 * Register override bindings for provided stubs (singletons!). Stub instances created by guice (to be able to use
 * injections inside it). If stub field is initialized manually - this value would be bound into guice context
 * (see debug report to be sure what value was actually used - field might be assigned too late).
 * <p>
 * In beforeAll injects static values, in beforeEach inject both (in case if beforeAll wasn't called).
 * <p>
 * For stub objects, implementing {@link ru.vyarus.dropwizard.guice.test.jupiter.ext.stub.StubLifecycle} before and
 * after methods called on beforeEach and afterEach to perform cleanups.
 *
 * @author Vyacheslav Rusakov
 * @since 07.02.2025
 */
public class StubsSupport extends AnnotatedTestFieldSetup<StubBean, Object> {

    // test context storage key for resolved fields
    private static final String TEST_STUB_FIELDS = "TEST_STUB_FIELDS";

    public StubsSupport() {
        super(StubBean.class, Object.class, TEST_STUB_FIELDS);
    }

    @Override
    protected void validateDeclaration(final ExtensionContext context,
                                       final AnnotatedField<StubBean, Object> field) {
        final Class<?> key = field.getAnnotation().value();
        final Class<Object> type = field.getType();
        if (!key.isAssignableFrom(type)) {
            throw new IllegalStateException(getDeclarationErrorPrefix(field) + type.getSimpleName()
                    + " is not assignable to " + key.getSimpleName());
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <K> void bindFieldValue(final Binder binder,
                                      final AnnotatedField<StubBean, Object> field, final Object value) {
        final Class<? super K> key = (Class<? super K>) field.getAnnotation().value();
        binder.bind(key).toInstance((K) value);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <K> void bindField(final Binder binder, final AnnotatedField<StubBean, Object> field) {
        final Class<? super K> key = (Class<? super K>) field.getAnnotation().value();
        // bind original type to stub - guice will instantiate it
        // IMPORTANT to bind as singleton - otherwise different instances would be everywhere
        binder.bind(key).to((Class<K>) field.getType()).in(Singleton.class);
    }

    @Override
    protected void validateBinding(final EventContext context, final AnnotatedField<StubBean, Object> field) {
        // nothing
    }

    @Override
    protected Object getFieldValue(final EventContext context, final AnnotatedField<StubBean, Object> field) {
        // if not declared, stub value created by guice
        return context.getBean(field.getAnnotation().value());
    }

    @Override
    @SuppressWarnings("PMD.SystemPrintln")
    protected void report(final EventContext context,
                          final List<AnnotatedField<StubBean, Object>> fields) {
        final StringBuilder report = new StringBuilder("\nApplied stubs (@")
                .append(StubBean.class.getSimpleName()).append(") on ").append(setupContextName).append(":\n\n");
        fields.forEach(field -> report.append(
                String.format("\t%-40s %-10s %20s >> %-20s%n",
                        field.getField().getDeclaringClass().getSimpleName() + "." + field.getField().getName(),
                        field.isCustomDataSet(FIELD_MANUAL) ? "MANUAL" : "GUICE",
                        field.getAnnotation().value().getSimpleName(),
                        field.getType().getSimpleName())));
        System.out.println(report);
    }

    @Override
    protected void beforeTest(final EventContext context,
                              final AnnotatedField<StubBean, Object> field,
                              final Object value) {
        if (value instanceof StubLifecycle) {
            ((StubLifecycle) value).before();
        }
    }

    @Override
    protected void afterTest(final EventContext context,
                             final AnnotatedField<StubBean, Object> field,
                             final Object value) {
        if (value instanceof StubLifecycle) {
            ((StubLifecycle) value).after();
        }
    }
}
