package ru.vyarus.dropwizard.guice.test.jupiter.ext.stub;

import com.google.inject.Binder;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import org.junit.jupiter.api.extension.ExtensionContext;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook;
import ru.vyarus.dropwizard.guice.test.jupiter.env.TestEnvironmentSetup;
import ru.vyarus.dropwizard.guice.test.jupiter.env.TestExecutionListener;
import ru.vyarus.dropwizard.guice.test.jupiter.env.TestExtension;
import ru.vyarus.dropwizard.guice.test.util.AnnotatedField;

import java.util.List;
import java.util.function.Consumer;

/**
 * {@link ru.vyarus.dropwizard.guice.test.jupiter.ext.stub.StubBean} test fields support implementation.
 * <p>
 * Annotated fields resolved in time of with guicey extension initialization (beforeAll or beforeEach).
 * Register override bindings for provided stubs (singletons!). Stub instances created by guice (to be able to use
 * injections inside it).
 * <p>
 * In beforeAll injects static values, in beforeEach inject both (in case if beforeAll wasn't called).
 * <p>
 * For stub objects, implementing {@link ru.vyarus.dropwizard.guice.test.jupiter.ext.stub.StubLifecycle} before and
 * after methods called on beforeEach and afterEach to perform cleanups.
 *
 * @author Vyacheslav Rusakov
 * @since 07.02.2025
 */
public class StubsSupport implements TestEnvironmentSetup, GuiceyConfigurationHook, TestExecutionListener {

    private boolean appPerClass;
    private List<AnnotatedField<StubBean, Object>> fields;

    @Override
    public Object setup(final TestExtension extension) {
        appPerClass = extension.isApplicationStartedForClass();
        // find all annotated fields in test class
        fields = extension.findAnnotatedFields(StubBean.class);

        if (extension.isDebug()) {
            reportStubs(fields);
        }

        return null;
    }

    @Override
    public void configure(final GuiceBundle.Builder builder) {
        if (!fields.isEmpty()) {
            // override real beans with stubs
            builder.modulesOverride(binder -> collectOverrideBindings(fields, binder));
        }
    }

    @Override
    public void beforeAll(final ExtensionContext context) {
        // inject static fields
        injectStubs(fields, null, getInjector(context));
    }

    @Override
    public void beforeEach(final ExtensionContext context) {
        // inject non-static fields
        final Object testInstance = context.getRequiredTestInstance();
        injectStubs(fields, testInstance, getInjector(context));
        // call lifecycle methods on stub if required
        stubLifecycle(fields, testInstance, StubLifecycle::before);
    }

    @Override
    public void afterEach(final ExtensionContext context) {
        // call lifecycle methods on stub if required
        stubLifecycle(fields, context.getRequiredTestInstance(), StubLifecycle::after);
    }

    @SuppressWarnings("unchecked")
    private <T> void collectOverrideBindings(final List<AnnotatedField<StubBean, Object>> fields, final Binder binder) {
        for (final AnnotatedField<StubBean, Object> field : fields) {
            final Class<? super T> key = (Class<? super T>) field.getAnnotation().value();
            // bind original type to stub - guice will instantiate it
            // IMPORTANT to bind as singleton - otherwise different instances would be everywhere
            final Class<Object> type = field.getType();
            if (!key.isAssignableFrom(type)) {
                throw new IllegalStateException("Incorrect @" + StubBean.class.getSimpleName()
                        + " '" + field.toStringField() + "' declaration: " + type.getSimpleName()
                        + " is not assignable to " + key.getSimpleName());
            }
            binder.bind(key).to((Class<T>) type).in(Singleton.class);
        }
    }

    private void stubLifecycle(final List<AnnotatedField<StubBean, Object>> fields,
                               final Object testInstance,
                               final Consumer<StubLifecycle> callback) {
        fields.forEach(field -> {
            final Object value = field.getValue(testInstance);
            if (value instanceof StubLifecycle) {
                callback.accept((StubLifecycle) value);
            }
        });
    }

    private void injectStubs(final List<AnnotatedField<StubBean, Object>> fields,
                             final Object testInstance,
                             final Injector injector) {
        fields.forEach(field -> {
            // inject static fields also in beforeEach if was not done in beforeAll
            if ((testInstance != null || (field.isStatic() && !appPerClass))) {
                field.setValue(testInstance, injector.getInstance(field.getAnnotation().value()));
            }
        });
    }

    @SuppressWarnings("PMD.SystemPrintln")
    private void reportStubs(final List<AnnotatedField<StubBean, Object>> fields) {
        if (!fields.isEmpty()) {
            final StringBuilder report = new StringBuilder("\nApplied stubs (@")
                    .append(StubBean.class.getSimpleName()).append("):\n");
            fields.forEach(field -> report.append(String.format("\t%-40s  %20s >> %-20s%n",
                    field.getField().getDeclaringClass().getSimpleName() + "." + field.getField().getName(),
                    field.getAnnotation().value().getSimpleName(),
                    field.getType().getSimpleName())));
            System.out.println(report);
        }
    }
}
