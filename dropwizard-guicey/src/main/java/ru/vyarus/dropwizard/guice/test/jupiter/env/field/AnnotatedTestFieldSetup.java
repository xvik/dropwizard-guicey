package ru.vyarus.dropwizard.guice.test.jupiter.env.field;

import com.google.inject.Binder;
import com.google.inject.Binding;
import com.google.inject.Stage;
import com.google.inject.spi.InstanceBinding;
import com.google.inject.spi.ProviderInstanceBinding;
import jakarta.inject.Provider;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstances;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook;
import ru.vyarus.dropwizard.guice.test.jupiter.env.TestEnvironmentSetup;
import ru.vyarus.dropwizard.guice.test.jupiter.env.TestExtension;
import ru.vyarus.dropwizard.guice.test.jupiter.env.listen.EventContext;
import ru.vyarus.dropwizard.guice.test.jupiter.env.listen.TestExecutionListener;
import ru.vyarus.dropwizard.guice.test.util.TestSetupUtils;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

/**
 * Base class for annotated test field extensions.
 * Encapsulates:
 * - fields detection and processing
 * - declaration validations
 * - nested tests support
 * - manual user values support
 * - injected field lifecycle support (if required to rest an object before each test)
 * - guice bindings report compatibility (when guice bindings evaluated the second time)
 * <p>
 * All methods are protected to simplify overriding logic if required.
 * <p>
 * Implementation could be registered as usual {@link ru.vyarus.dropwizard.guice.test.jupiter.env.TestEnvironmentSetup}
 * object (hook and listener would be registered automatically).
 *
 * @param <A> annotation type
 * @param <T> required field type for auto validations (could be Object)
 * @author Vyacheslav Rusakov
 * @since 10.02.2025
 */
@SuppressWarnings("PMD.TooManyMethods")
public abstract class AnnotatedTestFieldSetup<A extends Annotation, T> implements
        TestEnvironmentSetup, GuiceyConfigurationHook, TestExecutionListener {

    // field custom data
    protected static final String FIELD_MANUAL = "manual_creation";
    protected static final String FIELD_INJECTED = "value_injected";

    protected boolean appPerClass;
    protected Class<?> regTestClass;
    protected String setupContextName;
    protected List<AnnotatedField<A, T>> fields;
    protected ExtensionContext setupContext;

    // inner state
    private final Class<A> fieldAnnotation;
    private final Class<T> fieldType;
    // test context storage key for resolved fields
    private final String storageKey;

    // private to not confuse sub-classes with setup-only instances
    private TestInstances regTestInstance;

    /**
     * On extending, use a default constructor and specify required parameters manually.
     *
     * @param fieldAnnotation field annotation class
     * @param fieldType       required fields type (could be Object)
     * @param storageKey      key used to store a fields list in junit context (must be unique for extension)
     */
    public AnnotatedTestFieldSetup(final Class<A> fieldAnnotation,
                                   final Class<T> fieldType,
                                   final String storageKey) {
        this.fieldAnnotation = fieldAnnotation;
        this.fieldType = fieldType;
        this.storageKey = storageKey;
    }

    @Override
    public Object setup(final TestExtension extension) {
        appPerClass = extension.isApplicationStartedForClass();
        setupContext = extension.getJunitContext();
        regTestClass = setupContext.getRequiredTestClass();
        regTestInstance = setupContext.getTestInstances().orElse(null);
        this.setupContextName = TestSetupUtils.getContextTestName(setupContext);

        // find all annotated fields in test class (if not already found)
        // For nested test and guice extension per method initialization, all fields resolved for top classes
        // must also be included (otherwise their values would remain null)
        fields = lookupFields(setupContext, () -> extension.findAnnotatedFields(fieldAnnotation, fieldType));
        if (!fields.isEmpty()) {
            // avoid registration if no fields declared
            extension.hooks(this).listen(this);
        }
        return null;
    }

    @Override
    public void configure(final GuiceBundle.Builder builder) {
        // called immediately after setup - may use instance fields
        // configure guicey (most likely, override bindings)
        // override real beans with stubs
        builder.modulesOverride(binder -> collectOverrideBindings(fields, binder));
    }

    /**
     * Validate resolved field, if required. Note that some validations are performed automatically like
     * checking field type with provided required type or unreachable annotated fields reporting. This method
     * should be used for validations, which are not possible to perform automatically (e.g., there is a
     * class, declared in annotation that must comply with a field type (base class know nothing about annotation and
     * can't check that).
     * <p>
     * Called only for current test class own fields: in case of nested test, root test fields would already be
     * validated. Also, if guice context started per each test method, validation would be called only for the first
     * test method because fields would be searched just once - no need to validate each time.
     *
     * @param context junit context
     * @param field   annotated fields
     */
    protected abstract void validateDeclaration(ExtensionContext context, AnnotatedField<A, T> field);

    /**
     * Configure guice override for field with user value. Binder belongs to a new guice module, registered as
     * overriding
     * ({@link ru.vyarus.dropwizard.guice.GuiceBundle.Builder#modulesOverride(com.google.inject.Module...)}):
     * any binding for an existing application key will OVERRIDE it.
     * <p>
     * NOTE: If user-provided values are not allowed, throw an exception here
     *
     * @param binder guice overriding module binder
     * @param field  annotated field
     * @param value  user-provided value to bind
     * @param <K>    type for aligning a binding key with value types (cheating on guice type checks)
     */
    protected abstract <K> void bindFieldValue(Binder binder, AnnotatedField<A, T> field, T value);

    /**
     * Configure guice override for field with user value. Binder belongs to a new guice module, registered as
     * overriding
     * ({@link ru.vyarus.dropwizard.guice.GuiceBundle.Builder#modulesOverride(com.google.inject.Module...)}):
     * any binding for an existing application key will OVERRIDE it.
     *
     * @param binder guice overriding module binder
     * @param field  annotated field
     * @param <K>    type for aligning a binding key with value types (cheating on guice type checks)
     */
    protected abstract <K> void bindField(Binder binder, AnnotatedField<A, T> field);

    /**
     * Called after application startup to validate binding correctness. For example, to detect instance bindings
     * when extension relies on AOP and so would not work. Such validation is impossible to do before (in time
     * of binding overrides).
     * <p>
     * Called before {@link #getFieldValue(ru.vyarus.dropwizard.guice.test.jupiter.env.listen.EventContext,
     * AnnotatedField)}.
     *
     * @param context event context
     * @param field   annotated field
     */
    protected abstract void validateBinding(EventContext context, AnnotatedField<A, T> field);

    /**
     * Get test field value (would be immediately injected into the test field). Called only if field was not
     * initialized by user (not a manual value). For example, implementation might simply get bean instance
     * from guice context (if guice was re-configured with module overrides).
     * <p>
     * Warning: not called for manually initialized fields (because value already set)! To validate binding use
     * {@link #validateBinding(ru.vyarus.dropwizard.guice.test.jupiter.env.listen.EventContext, AnnotatedField)}
     * method instead (which is called for all fields).
     * <p>
     * Application already completely started and test extension initialized at this moment (beforeEach test phase).
     *
     * @param context event context
     * @param field   annotated field
     * @return created field value
     */
    protected abstract T getFieldValue(EventContext context, AnnotatedField<A, T> field);

    /**
     * Called when debug is enabled on guicey extension to report registered fields.
     * Note: there might be fields from multiple test classes in case of nested tests.
     * <p>
     * Report called after application startup because at this point all fields were processed (in configure guice
     * method) and so all required fields data collected. Called only if at least one field is detected.
     * <p>
     * Special custom data markers used in field objects
     * ({@link AnnotatedField#getCustomData(String)}):
     * - {@link #FIELD_MANUAL} field value was initialized by user, otherwise automatic
     * - {@link #FIELD_INJECTED} field injection instance (test instance)
     *
     * @param context event context, IMPORTANT - this would be setup context and not current
     * @param fields  fields to report
     */
    protected abstract void report(EventContext context, List<AnnotatedField<A, T>> fields);

    /**
     * Called before each test to pre-process field value (if required).
     *
     * @param context event context
     * @param field   filed descriptor
     * @param value   value instance
     */
    protected abstract void beforeTest(EventContext context, AnnotatedField<A, T> field, T value);

    /**
     * Called after each test to post-process field value (if required).
     *
     * @param context event context
     * @param field   filed descriptor
     * @param value   value instance
     */
    protected abstract void afterTest(EventContext context, AnnotatedField<A, T> field, T value);

    @Override
    public void started(final EventContext context) {
        // here because manual stubs detection will appear only during injector startup
        if (context.isDebug() && !fields.isEmpty()) {
            report(new EventContext(setupContext, true), fields);
        }
    }

    @Override
    public void beforeAll(final EventContext context) {
        // inject static fields
        final Class<?> testClass = context.getJunitContext().getRequiredTestClass();
        if (testClass == regTestClass) {
            injectValues(context, fields, null);
        } else {
            // in case on nested tests - search for declared fields and fail because injector already created
            validateUnreachableFieldsInNestedTest(testClass);
        }
    }

    @Override
    public void beforeEach(final EventContext context) {
        // inject non-static fields
        final TestInstances testInstances = context.getJunitContext().getRequiredTestInstances();
        injectValues(context, fields, testInstances);
        // call lifecycle methods on stub if required
        valueLifecycle(context, fields, testInstances, true);
    }

    @Override
    public void afterEach(final EventContext context) {
        // call lifecycle methods on stub if required
        valueLifecycle(context, fields, context.getJunitContext().getRequiredTestInstances(), false);
    }

    @Override
    public void stopped(final EventContext context) {
        // after app shutdown clear static fields injected with guice-managed bean
        // otherwise it would be impossible to differentiate it from manual stub for the next test (on per method)
        fields.forEach(field -> {
            if (field.isStatic() && !field.isCustomDataSet(FIELD_MANUAL)) {
                field.setValue(null, null);
                field.clearCustomData();
            }
        });
    }

    /**
     * Resolve test own fields or use already resolved fields set (for example, when guicey extension created
     * for each method we can search and validate fields just once). Also adds fields from all paren contexts
     * (for nested tests, which must see parent test fields and so we need to manage its lifecycle)
     *
     * @param context        junit context
     * @param fieldsProvider fields searching logic
     * @return all annotated fields
     */
    protected List<AnnotatedField<A, T>> lookupFields(
            final ExtensionContext context,
            final Provider<List<AnnotatedField<A, T>>> fieldsProvider) {
        final ExtensionContext ctx = getClassContext(context);

        // resolved fields are always stored under CLASS context
        // so if extension created per method it would analyze fields just once
        // Nested tests would also use already prepared parent fields (when extension created per method)
        List<AnnotatedField<A, T>> res = getOwnFields(ctx);
        if (res == null) {
            res = fieldsProvider.get();
            if (!res.isEmpty()) {
                // validate only own fields - top level fields assumed to be already validated (we are inside nested
                // test)
                res.forEach(field -> validateDeclaration(context, field));
            }
            getStore(ctx).put(storageKey, res);
        }

        // now looking for fields stored in parent contexts and adding all them (with state reset)
        final List<AnnotatedField<A, T>> inherited = getParentFields(ctx);
        // reset parent state!
        inherited.forEach(AnnotatedField::clearCustomData);
        res.addAll(inherited);
        return res;
    }

    /**
     * Create bind for annotated fields in overriding guice module. For user-provided values,
     * value binding should be used. For not initialized fields - value must be created (e.g., by guice context).
     *
     * @param fields annotated fields
     * @param binder overriding module binder
     */
    @SuppressWarnings("PMD.SimplifiedTernary")
    protected void collectOverrideBindings(final List<AnnotatedField<A, T>> fields, final Binder binder) {
        // binder might be called the second time for guice report - in this case all changes must be denied
        // (only bindings required to show in the report, but field changes would be incorrect anyway)
        final boolean ignoreChanges = binder.currentStage() == Stage.TOOL;
        for (final AnnotatedField<A, T> field : fields) {
            field.setIgnoreChanges(ignoreChanges);
            // look if field already initialized
            if (regTestInstance != null || field.isStatic()) {
                final T existing = field.getValue(regTestInstance);
                if (existing != null) {
                    // use manually initialized instance (field value)
                    bindFieldValue(binder, field, existing);
                    field.setCustomData(FIELD_MANUAL, true);
                    // mark static value as injected: no need to re-inject
                    field.setCustomData(FIELD_INJECTED, field.isStatic() ? true
                            : field.findRequiredInstance(regTestInstance));
                    continue;
                }
            }

            // bind as type - guice will create instance
            bindField(binder, field);
            field.setIgnoreChanges(false);
        }
    }

    /**
     * Inject field values into test instance (under beforeEach). User defined values stay as is. Value is injected
     * only once for test instance.
     *
     * @param context       event context
     * @param fields        annotated fields
     * @param testInstances tests instances (might be several for nested tests)
     */
    @SuppressWarnings({"CyclomaticComplexity", "PMD.SimplifiedTernary"})
    protected void injectValues(final EventContext context,
                                final List<AnnotatedField<A, T>> fields,
                                final TestInstances testInstances) {
        final boolean checkFieldValueInvisibleOnInitialization = testInstances != null && appPerClass;
        fields.forEach(field -> {
            if (checkFieldValueInvisibleOnInitialization && !field.isStatic()) {
                // when injector created in beforeAll, it can't see instance fields, but later we can validate
                // and fail on wrong usage
                failIfInstanceFieldInitialized(field, testInstances);
            }
            validateBinding(context, field);

            // exact instance required because it must be stored
            final Object instance = field.findRequiredInstance(testInstances);
            final boolean isAlreadyInjected = (field.isStatic() && field.isCustomDataSet(FIELD_INJECTED))
                    // instance check required because field might be used in multiple test instances during
                    // injector lifetime
                    || (!field.isStatic() && instance == field.getCustomData(FIELD_INJECTED));
            // static fields might be not initialized in beforeAll (so do it in beforeEach)
            if ((instance != null || field.isStatic()) && !isAlreadyInjected) {
                field.setValue(instance, getFieldValue(context, field));
                field.setCustomData(FIELD_INJECTED, field.isStatic() ? true : instance);
            }
        });
    }

    /**
     * Called in beforeEach/afterEach to apply automatic lifecycle for field objects.
     *
     * @param context       junit context
     * @param fields        annotated fields
     * @param testInstances test instances (might be several for nested tests)
     * @param before        true for beforeEach, false for afterEach
     */
    protected void valueLifecycle(final EventContext context,
                                  final List<AnnotatedField<A, T>> fields,
                                  final TestInstances testInstances,
                                  final boolean before) {
        fields.forEach(field -> {
            // value might be re-assigned in test setup method, but such change could be detected only after test:
            // throwing error to, at least, indicate problem (otherwise would be a confusion point)
            final T value = field.checkValueNotChanged(testInstances);
            if (before) {
                beforeTest(context, field, value);
            } else {
                afterTest(context, field, value);
            }
        });
    }

    /**
     * @param field field
     * @return universal prefix for field declaration errors
     */
    protected String getDeclarationErrorPrefix(final AnnotatedField<A, T> field) {
        return "Incorrect @" + fieldAnnotation.getSimpleName() + " '" + field.toStringField() + "' declaration: ";
    }

    /**
     * When guicey extension starts in beforeAll - it can't see instance fields (by default) and so can't check
     * if use provide any value. On beforeEach we have to validate that value was not provided, because it's too late -
     * guice context was already created.
     *
     * @param field         field to check
     * @param testInstances test instances (might be several in case of nested tests)
     */
    protected void failIfInstanceFieldInitialized(final AnnotatedField<A, T> field,
                                                  final TestInstances testInstances) {
        final Object value = field.getValue(testInstances);
        if (value != null && !field.isCustomDataSet(FIELD_INJECTED)) {
            throw new IllegalStateException(getDeclarationErrorPrefix(field) + "field value can't be used because "
                    + "guice context starts in beforeAll phase. Either make field static or remove value ("
                    + "guice will create instance with guice injector)");
        }
    }

    /**
     * When guicey extension created in beforeAll, same extension would be used for nested tests, which means
     * that, if nested test declares any annotated fields, they can't be injected into already started guice
     * context and so usage error must be reported.
     *
     * @param testClass nested test class
     */
    protected void validateUnreachableFieldsInNestedTest(final Class<?> testClass) {
        final List<AnnotatedField<A, T>> wrongFields = TestFieldUtils
                .findAnnotatedFields(testClass, fieldAnnotation, fieldType);
        if (!wrongFields.isEmpty()) {
            throw new IllegalStateException(getDeclarationErrorPrefix(wrongFields.get(0)) + "nested test runs under "
                    + "already started application and so new fields could not be added. Either remove annotated"
                    + " fields in nested tests or run application for each test method (with non-static "
                    + "@RegisterExtension field)");
        }
    }

    /**
     * This is important for the nested tests - each nested test may have its own set of fields (if guicey extension
     * created per test method). When guicey extension is created for nested test, we still need to resolve
     * parent test fields to correctly initialize them and apply value lifecycle (because nested test could see parent
     * fields).
     *
     * @param context junit context
     * @return all fields in parent contexts
     */
    protected List<AnnotatedField<A, T>> getParentFields(final ExtensionContext context) {
        final List<AnnotatedField<A, T>> res = new ArrayList<>();
        ExtensionContext ctx = context.getParent().orElse(null);
        while (ctx != null && ctx.getTestClass().isPresent()) {
            final List<AnnotatedField<A, T>> tmp = getOwnFields(ctx);
            if (tmp != null) {
                res.addAll(tmp);
            }
            ctx = ctx.getParent().orElse(null);
        }

        return res;
    }

    /**
     * @param context junit context
     * @return fields which belong to test class (ignoring fields in parent contexts)
     */
    @SuppressWarnings("unchecked")
    protected List<AnnotatedField<A, T>> getOwnFields(final ExtensionContext context) {
        return (List<AnnotatedField<A, T>>) getStore(context).get(storageKey);
    }

    /**
     * Gets test-specific extension storage.
     *
     * @param context junit context (class level!)
     * @return extension storage object
     */
    protected ExtensionContext.Store getStore(final ExtensionContext context) {
        // IMPORTANT getClass() used to store under different keys for different extensions
        return context.getStore(ExtensionContext.Namespace.create(getClass(), context.getRequiredTestClass()));
    }

    /**
     * Class context is the same for all test methods, and so it is suitable for storing something that must survive
     * between test methods.
     *
     * @param context junit context
     * @return class junit context
     */
    protected ExtensionContext getClassContext(final ExtensionContext context) {
        ExtensionContext ctx = context;
        while (ctx.getTestMethod().isPresent()) {
            ctx = ctx.getParent().get();
        }
        return ctx;
    }

    /**
     * Note: covers only the simplest cases (just for a basic validations).
     *
     * @param binding binding
     * @return true if guice does not manage bean instance, false otherwise
     */
    protected boolean isInstanceBinding(final Binding<?> binding) {
        // Note: there might be other situations, like guice-managed provider, providing instances
        // or longer binding chain. All cases are not checked intentionally - just the most obvious
        return binding instanceof InstanceBinding<?> || binding instanceof ProviderInstanceBinding<?>;
    }
}
