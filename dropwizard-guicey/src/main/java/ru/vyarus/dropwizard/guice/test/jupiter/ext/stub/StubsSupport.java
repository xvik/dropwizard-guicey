package ru.vyarus.dropwizard.guice.test.jupiter.ext.stub;

import com.google.inject.Binder;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import jakarta.inject.Provider;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstances;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook;
import ru.vyarus.dropwizard.guice.test.jupiter.env.TestEnvironmentSetup;
import ru.vyarus.dropwizard.guice.test.jupiter.env.TestExecutionListener;
import ru.vyarus.dropwizard.guice.test.jupiter.env.TestExtension;
import ru.vyarus.dropwizard.guice.test.util.AnnotatedField;
import ru.vyarus.dropwizard.guice.test.util.TestFieldUtils;
import ru.vyarus.dropwizard.guice.test.util.TestSetupUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * {@link ru.vyarus.dropwizard.guice.test.jupiter.ext.stub.StubBean} test fields support implementation.
 * <p>
 * Annotated fields resolved in time of with guicey extension initialization (beforeAll or beforeEach).
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
public class StubsSupport implements TestEnvironmentSetup, GuiceyConfigurationHook, TestExecutionListener {

    // test context storage key for resolved fields
    private static final String TEST_STUB_FIELDS = "TEST_STUB_FIELDS";

    // field custom data
    private static final String FIELD_MANUAL = "manual_creation";
    private static final String FIELD_INJECTED = "value_injected";

    private boolean debug;
    private boolean appPerClass;
    private Class<?> regTestClass;
    private TestInstances regTestInstance;
    private String context;
    private List<AnnotatedField<StubBean, Object>> fields;

    @Override
    public Object setup(final TestExtension extension) {
        appPerClass = extension.isApplicationStartedForClass();
        final ExtensionContext context = extension.getJunitContext();
        regTestClass = context.getRequiredTestClass();
        regTestInstance = context.getTestInstances().orElse(null);
        // for report
        debug = extension.isDebug();
        this.context = TestSetupUtils.getContextTestName(context);

        // find all annotated fields in test class (if not already found)
        // For nested test and guice extension per method initialization, all fields resolved for top classes
        // must also be included (otherwise their values would remain null)
        fields = lookupFields(context, () -> extension.findAnnotatedFields(StubBean.class));
        return null;
    }

    @Override
    public void configure(final GuiceBundle.Builder builder) {
        // called immediately after setup - may use instance fields
        if (!fields.isEmpty()) {
            // override real beans with stubs
            builder.modulesOverride(binder -> collectOverrideBindings(fields, binder));
        }
    }

    @Override
    public void started(final ExtensionContext context) {
        // here because manual stubs detection will appear only during injector startup
        if (debug) {
            reportStubs(fields);
        }
    }

    @Override
    public void beforeAll(final ExtensionContext context) {
        // inject static fields
        final Class<?> testClass = context.getRequiredTestClass();
        if (testClass == regTestClass) {
            injectStubs(fields, null, getInjector(context));
        } else {
            // in case on nested tests - search for declared fields and fail because injector already created
            failForStubFieldsInNestedTest(testClass);
        }
    }

    @Override
    public void beforeEach(final ExtensionContext context) {
        // inject non-static fields
        final TestInstances testInstances = context.getRequiredTestInstances();
        injectStubs(fields, testInstances, getInjector(context));
        // call lifecycle methods on stub if required
        stubLifecycle(fields, testInstances, StubLifecycle::before);
    }

    @Override
    public void afterEach(final ExtensionContext context) {
        // call lifecycle methods on stub if required
        stubLifecycle(fields, context.getRequiredTestInstances(), StubLifecycle::after);
    }

    @Override
    public void stopped(final ExtensionContext context) {
        // after app shutdown clear static fields injected with guice-managed bean
        // otherwise it would be impossible to differentiate it from manual stub for the next test (on per method)
        fields.forEach(field -> {
            if (field.isStatic() && !field.isCustomDataSet(FIELD_MANUAL)) {
                field.setValue(null, null);
                field.clearCustomData();
            }
        });
    }

    @SuppressWarnings({"unchecked", "PMD.SimplifiedTernary"})
    private <T> void collectOverrideBindings(final List<AnnotatedField<StubBean, Object>> fields, final Binder binder) {
        for (final AnnotatedField<StubBean, Object> field : fields) {
            validateConsistency(field);
            final Class<? super T> key = (Class<? super T>) field.getAnnotation().value();

            // look if field already initialized
            if (regTestInstance != null || field.isStatic()) {
                final T existing = (T) field.getValue(regTestInstance);
                if (existing != null) {
                    // use manually initialized instance (field value)
                    binder.bind(key).toInstance(existing);
                    field.setCustomData(FIELD_MANUAL, true);
                    // mark static value as injected: no need to re-inject
                    field.setCustomData(FIELD_INJECTED, field.isStatic() ? true
                            : field.findRequiredInstance(regTestInstance));
                    continue;
                }
            }

            // bind original type to stub - guice will instantiate it
            // IMPORTANT to bind as singleton - otherwise different instances would be everywhere
            binder.bind(key).to((Class<T>) field.getType()).in(Singleton.class);
        }
    }

    private void stubLifecycle(final List<AnnotatedField<StubBean, Object>> fields,
                               final TestInstances testInstances,
                               final Consumer<StubLifecycle> callback) {
        fields.forEach(field -> {
            final Object value = field.getValue(testInstances);
            if (value instanceof StubLifecycle) {
                callback.accept((StubLifecycle) value);
            }
        });
    }

    @SuppressWarnings({"CyclomaticComplexity", "PMD.SimplifiedTernary"})
    private void injectStubs(final List<AnnotatedField<StubBean, Object>> fields,
                             final TestInstances testInstances,
                             final Injector injector) {
        final boolean checkFieldValueInvisibleOnInitialization = testInstances != null && appPerClass;
        fields.forEach(field -> {
            if (checkFieldValueInvisibleOnInitialization && !field.isStatic()) {
                failIfInstanceFieldInitialized(field, testInstances);
            }

            // exact instance required because it must be stored
            final Object instance = field.findRequiredInstance(testInstances);
            final boolean isAlreadyInjected = (field.isStatic() && field.isCustomDataSet(FIELD_INJECTED))
                    || (!field.isStatic() && instance == field.getCustomData(FIELD_INJECTED));
            // static fields might be not initialized in beforeAll (so do it in beforeEach)
            if ((instance != null || field.isStatic()) && !isAlreadyInjected) {
                field.setValue(instance, injector.getInstance(field.getAnnotation().value()));
                field.setCustomData(FIELD_INJECTED, field.isStatic() ? true : instance);
            }
        });
    }

    private void validateConsistency(final AnnotatedField<StubBean, Object> field) {
        final Class<?> key = field.getAnnotation().value();
        final Class<Object> type = field.getType();
        if (!key.isAssignableFrom(type)) {
            throw new IllegalStateException(errorPrefix(field) + type.getSimpleName()
                    + " is not assignable to " + key.getSimpleName());
        }
    }

    private void failIfInstanceFieldInitialized(final AnnotatedField<StubBean, Object> field,
                                                final TestInstances testInstances) {
        final Object value = field.getValue(testInstances);
        if (value != null && !field.isCustomDataSet(FIELD_INJECTED)) {
            throw new IllegalStateException(errorPrefix(field) + "field value can't be used as stub because "
                    + "guice context starts in beforeAll phase. Either make field static or remove value ("
                    + "guice will create stub instance with guice injector)");
        }
    }

    private void failForStubFieldsInNestedTest(final Class<?> testClass) {
        final List<AnnotatedField<StubBean, Object>> stubFields = TestFieldUtils
                .findAnnotatedFields(testClass, StubBean.class, Object.class);
        if (!stubFields.isEmpty()) {
            throw new IllegalStateException(errorPrefix(stubFields.get(0)) + "nested test runs under "
                    + "already started application and so new stubs could not be added. Either remove stubs in nested"
                    + " tests or run application for each test method (with non-static @RegisterExtension field)");
        }
    }

    private String errorPrefix(final AnnotatedField<StubBean, Object> field) {
        return "Incorrect @" + StubBean.class.getSimpleName() + " '" + field.toStringField() + "' declaration: ";
    }

    @SuppressWarnings("PMD.SystemPrintln")
    private void reportStubs(final List<AnnotatedField<StubBean, Object>> fields) {
        if (!fields.isEmpty()) {
            final StringBuilder report = new StringBuilder("\nApplied stubs (@")
                    .append(StubBean.class.getSimpleName()).append(") on ").append(context).append(":\n");
            fields.forEach(field -> report.append(
                    String.format("\t%-40s %-10s %20s >> %-20s%n",
                            field.getField().getDeclaringClass().getSimpleName() + "." + field.getField().getName(),
                            field.isCustomDataSet(FIELD_MANUAL) ? "MANUAL" : "GUICE",
                            field.getAnnotation().value().getSimpleName(),
                            field.getType().getSimpleName())));
            System.out.println(report);
        }
    }

    private List<AnnotatedField<StubBean, Object>> lookupFields(
            final ExtensionContext context,
            final Provider<List<AnnotatedField<StubBean, Object>>> fieldsProvider) {
        final ExtensionContext ctx = getClassContext(context);

        // resolved fields are always stored under CLASS context
        // so if extension created per method it would analyze fields just once
        // Nested tests would also use already prepared parent stub fields (when extension created per method)
        List<AnnotatedField<StubBean, Object>> res = getOwnFields(ctx);
        if (res == null) {
            res = fieldsProvider.get();
            getStore(ctx).put(TEST_STUB_FIELDS, res);
        }

        // now looking for fields stored in parent contexts and adding all them (with state reset)
        final List<AnnotatedField<StubBean, Object>> inherited = getParentFields(ctx);
        // reset parent state!
        inherited.forEach(AnnotatedField::clearCustomData);
        res.addAll(inherited);
        return res;
    }

    private List<AnnotatedField<StubBean, Object>> getParentFields(final ExtensionContext context) {
        final List<AnnotatedField<StubBean, Object>> res = new ArrayList<>();
        ExtensionContext ctx = context.getParent().orElse(null);
        while (ctx != null && ctx.getTestClass().isPresent()) {
            final List<AnnotatedField<StubBean, Object>> tmp = getOwnFields(ctx);
            if (tmp != null) {
                res.addAll(tmp);
            }
            ctx = ctx.getParent().orElse(null);
        }

        return res;
    }

    @SuppressWarnings("unchecked")
    private List<AnnotatedField<StubBean, Object>> getOwnFields(final ExtensionContext context) {
        return (List<AnnotatedField<StubBean, Object>>) getStore(context).get(TEST_STUB_FIELDS);
    }

    private ExtensionContext.Store getStore(final ExtensionContext context) {
        return context.getStore(ExtensionContext.Namespace
                .create(StubsSupport.class, context.getRequiredTestClass()));
    }

    private ExtensionContext getClassContext(final ExtensionContext context) {
        ExtensionContext ctx = context;
        while (ctx.getTestMethod().isPresent()) {
            ctx = ctx.getParent().get();
        }
        return ctx;
    }
}
