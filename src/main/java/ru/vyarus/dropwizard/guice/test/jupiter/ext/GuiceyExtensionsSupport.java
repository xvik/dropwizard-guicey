package ru.vyarus.dropwizard.guice.test.jupiter.ext;

import com.google.common.base.Preconditions;
import com.google.inject.Injector;
import io.dropwizard.testing.DropwizardTestSupport;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.support.AnnotationSupport;
import org.junit.platform.commons.util.ReflectionUtils;
import ru.vyarus.dropwizard.guice.hook.ConfigurationHooksSupport;
import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook;
import ru.vyarus.dropwizard.guice.injector.lookup.InjectorLookup;
import ru.vyarus.dropwizard.guice.test.ClientSupport;
import ru.vyarus.dropwizard.guice.test.EnableHook;
import ru.vyarus.dropwizard.guice.test.jupiter.env.EnableSetup;
import ru.vyarus.dropwizard.guice.test.jupiter.env.TestEnvironmentSetup;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.conf.TestExtensionsTracker;
import ru.vyarus.dropwizard.guice.test.util.ConfigOverrideUtils;
import ru.vyarus.dropwizard.guice.test.util.HooksUtil;
import ru.vyarus.dropwizard.guice.test.util.TestSetupUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Base class for junit 5 extensions implementations. All extensions use {@link DropwizardTestSupport} object
 * for actual execution (only configuration differs).
 * <p>
 * Extensions might be used on class level (annotation and manual registration in static field; when extension start
 * dropwizard app before all tests and shut down it after all tests) and on method level (manual registration in non
 * static field; application starts before each test).
 * <p>
 * Nested tests also supported.
 * <p>
 * Test instance is not managed by guice! Only {@link com.google.inject.Injector#injectMembers(Object)} applied
 * for it to process test fields injection. Guice AOP can't be used on test methods. Technically, creating test
 * instances with guice is possible, but in this case nested tests could not work at all, which is unacceptable.
 * <p>
 * Extension detects static fields of {@link GuiceyConfigurationHook} type, annotated with {@link EnableHook}
 * and initialize these hooks automatically. It was done like this to simplify customizations, when main extension
 * could be declared as annotation and hook as field. Also, it was impossible to implement hooks support
 * with junit extension. Hook field could be declared even in base test class.
 * <p>
 * Also, detects {@link ru.vyarus.dropwizard.guice.test.jupiter.env.TestEnvironmentSetup} fields annotated with
 * {@link ru.vyarus.dropwizard.guice.test.jupiter.env.EnableSetup}. Behaviour is the same as with hook fields.
 * <p>
 * For external integrations (other extensions), there is a special "hack" allowing to access
 * {@link DropwizardTestSupport} object (and so get access to injector): {@link #lookupSupport(ExtensionContext)}.
 * And shortcuts {@link #lookupInjector(ExtensionContext)} and {@link #lookupClient(ExtensionContext)}.
 *
 * @author Vyacheslav Rusakov
 * @see TestParametersSupport for supported test parameters
 * @since 29.04.2020
 */
public abstract class GuiceyExtensionsSupport extends TestParametersSupport implements
        BeforeAllCallback,
        AfterAllCallback,
        BeforeEachCallback,
        AfterEachCallback {

    // dropwizard support storage key (store visible for all relative tests)
    private static final String DW_SUPPORT = "DW_SUPPORT";
    // ClientFactory instance
    private static final String DW_CLIENT = "DW_CLIENT";
    // indicator storage key of nested test (when extension activated in parent test)
    private static final String INHERITED_DW_SUPPORT = "INHERITED_DW_SUPPORT";
    // indicator storage key for case when application started for each method in test
    private static final String PER_METHOD_DW_SUPPORT = "PER_METHOD_DW_SUPPORT";

    protected final TestExtensionsTracker tracker;

    public GuiceyExtensionsSupport(final TestExtensionsTracker tracker) {
        this.tracker = tracker;
    }

    @Override
    public void beforeAll(final ExtensionContext context) throws Exception {
        if (!lookupSupport(context).isPresent()) {
            start(context, null);
        } else {
            // in case of nested test, beforeAll for root extension will be called second time (because junit keeps
            // only one extension instance!) and this means we should not perform initialization, but we also must
            // prevent afterAll call for this nested test too and so need to store marker value!

            final ExtensionContext.Store localStore = getLocalExtensionStore(context);
            // just in case
            Preconditions.checkState(localStore.get(INHERITED_DW_SUPPORT) == null,
                    "Storage assumptions were wrong or unexpected junit usage appear. "
                            + "Please report this case to guicey developer.");
            localStore.put(INHERITED_DW_SUPPORT, true);
        }
    }

    @Override
    public void beforeEach(final ExtensionContext context) throws Exception {
        // run-per-method support (activated with @RegisterExtension on non-static field only)
        if (!lookupSupport(context).isPresent()) {
            start(context, context.getTestInstance().get());
            // mark per-method mode to properly shut down after test method
            getLocalExtensionStore(context).put(PER_METHOD_DW_SUPPORT, true);
        }

        // before each used to properly handle both default @TestInstance(TestInstance.Lifecycle.PER_METHOD)
        // and @TestInstance(TestInstance.Lifecycle.PER_CLASS) (in later case BeforeAllCallback called after
        // TestInstancePostProcessor, making it not usable for this task)

        final Object testInstance = context.getTestInstance()
                .orElseThrow(() -> new IllegalStateException("Unable to get the current test instance"));

        final DropwizardTestSupport<?> support = Preconditions.checkNotNull(getSupport(context),
                "Guicey test support was not initialized: most likely, you are trying to manually "
                        + "register extension using non-static field - such usage is not supported.");

        InjectorLookup.getInjector(support.getApplication()).orElseThrow(() ->
                        new IllegalStateException("Can't find guicey injector to process test fields injections"))
                .injectMembers(testInstance);
    }

    @Override
    public void afterEach(final ExtensionContext context) throws Exception {
        if (getLocalExtensionStore(context).get(PER_METHOD_DW_SUPPORT) != null) {
            stop(context);
        }
    }

    @Override
    public void afterAll(final ExtensionContext context) throws Exception {
        // do nothing in application per test method mode
        if (lookupSupport(context).isPresent()) {

            // nested tests support
            final Object nestedTestMarker = getLocalExtensionStore(context).remove(INHERITED_DW_SUPPORT);
            if (nestedTestMarker != null) {
                // do nothing: extension managed on upper context
                return;
            }

            stop(context);
        }
    }

    // --------------------------------------------------------- 3rd party extensions support

    /**
     * Static "hack" for other extensions extending base guicey extensions abilities.
     * <p>
     * The only thin moment here is extensions order! Junit preserve declaration order so in most cases it
     * should not be a problem.
     *
     * @param extensionContext extension context
     * @return dropwizard support object prepared by guicey extension, or null if no guicey extension used or
     * its beforeAll hook was not called yet
     */
    public static Optional<DropwizardTestSupport<?>> lookupSupport(final ExtensionContext extensionContext) {
        return Optional.ofNullable((DropwizardTestSupport<?>) getExtensionStore(extensionContext).get(DW_SUPPORT));
    }

    /**
     * Shortcut for application injector resolution be used by other extensions.
     * <p>
     * Custom extension must be activated after main guicey extension!
     *
     * @param extensionContext extension context
     * @return application injector or null if not available
     */
    public static Optional<Injector> lookupInjector(final ExtensionContext extensionContext) {
        return lookupSupport(extensionContext).flatMap(it -> InjectorLookup.getInjector(it.getApplication()));
    }

    /**
     * Shortcut for {@link ClientSupport} object lookup by other extensions.
     * <p>
     * Custom extension must be activated after main guicey extension!
     *
     * @param extensionContext extension context
     * @return client factory object or null if not available
     */
    public static Optional<ClientSupport> lookupClient(final ExtensionContext extensionContext) {
        return Optional.ofNullable((ClientSupport) getExtensionStore(extensionContext).get(DW_CLIENT));
    }

    // --------------------------------------------------------- end of 3rd party extensions support

    /**
     * The only role of actual extension class is to configure {@link DropwizardTestSupport} object
     * according to annotation.
     *
     * @param configPrefix configuration properties prefix
     * @param context extension context
     * @param setups  setup extensions resolved from fields (or empty list)
     * @return configured dropwizard test support object
     */
    protected abstract DropwizardTestSupport<?> prepareTestSupport(String configPrefix,
                                                                   ExtensionContext context,
                                                                   List<TestEnvironmentSetup> setups);

    @Override
    protected DropwizardTestSupport<?> getSupport(final ExtensionContext extensionContext) {
        return lookupSupport(extensionContext).orElse(null);
    }

    @Override
    protected ClientSupport getClient(final ExtensionContext extensionContext) {
        return lookupClient(extensionContext).orElse(null);
    }

    @Override
    protected Optional<Injector> getInjector(final ExtensionContext extensionContext) {
        return lookupInjector(extensionContext);
    }

    protected static ExtensionContext.Store getExtensionStore(final ExtensionContext context) {
        // Store is extension specific, but nested tests will see it too (because key is extension class)
        return context.getStore(ExtensionContext.Namespace.create(GuiceyExtensionsSupport.class));
    }

    private ExtensionContext.Store getLocalExtensionStore(final ExtensionContext context) {
        // test scoped extension scope (required to differentiate nested classes or parameterized executions)
        return context.getStore(ExtensionContext.Namespace
                .create(GuiceyExtensionsSupport.class, context.getRequiredTestClass()));
    }

    private void start(final ExtensionContext context, final Object testInstance) throws Exception {
        final ExtensionContext.Store store = getExtensionStore(context);
        // find fields annotated with @EnableHook and @EnableSetup
        final FieldSupport fields = new FieldSupport(context.getRequiredTestClass(), testInstance, tracker);
        fields.activateBaseHooks();

        // config overrides work through system properties so it is important to have unique prefixes
        final String configPrefix = ConfigOverrideUtils.createPrefix(context);
        final DropwizardTestSupport<?> support = prepareTestSupport(configPrefix, context, fields.getSetupObjects());
        // activate hooks declared in test static fields (so hooks declared in annotation goes before)
        fields.activateClassHooks();
        store.put(DW_SUPPORT, support);
        // for pure guicey tests client may seem redundant, but it can be used for calling other services
        store.put(DW_CLIENT, new ClientSupport(support));

        tracker.enableDebugFromSystemProperty();
        tracker.logUsedHooksAndSetupObjects(configPrefix);
        support.before();
        tracker.logOverriddenConfigs(configPrefix);
    }

    private void stop(final ExtensionContext context) throws Exception {
        // just in case, normally hooks cleared automatically after appliance
        ConfigurationHooksSupport.reset();

        final DropwizardTestSupport<?> support = getSupport(context);
        if (support != null) {
            support.after();
        }
        final ClientSupport client = getClient(context);
        if (client != null) {
            client.close();
        }
    }

    /**
     * Utility class for activating hooks and setup objects collected from fields (annotated with
     * {@link EnableHook} and {link {@link ru.vyarus.dropwizard.guice.test.jupiter.env.EnableSetup}}).
     * <p>
     * Hook fields must be activated in two steps: first hooks declared in base classes, then hooks declared directly
     * in test class (after hooks declared in extension would be activated).
     * <p>
     * Setup extensions from fields are always registered after all other.
     */
    private static class FieldSupport {
        // test instance in application per test method case (beforeEach)
        private final Object instance;
        private final TestExtensionsTracker tracker;
        private final List<Field> parentHookFields;
        private final List<Field> ownHookFields;
        private final List<Field> extensionFields;

        FieldSupport(final Class<?> testClass, final Object instance, final TestExtensionsTracker tracker) {
            this.instance = instance;
            this.tracker = tracker;
            // find and validate all fields
            final boolean includeInstanceFields = instance != null;
            ownHookFields = findHookFields(testClass, includeInstanceFields);
            parentHookFields = ownHookFields.isEmpty() ? Collections.emptyList() : ownHookFields.stream()
                    .filter(field -> !testClass.equals(field.getDeclaringClass()))
                    .collect(Collectors.toList());
            ownHookFields.removeAll(parentHookFields);

            extensionFields = findSetupFields(testClass, includeInstanceFields);
        }

        public List<TestEnvironmentSetup> getSetupObjects() {
            tracker.extensionsFromFields(extensionFields, instance);
            return extensionFields.isEmpty() ? Collections.emptyList() : getFieldValues(extensionFields);
        }

        public void activateBaseHooks() {
            // activate hooks declared in base classes
            activateFieldHooks(parentHookFields);
            tracker.hooksFromFields(parentHookFields, true, instance);
        }

        public void activateClassHooks() {
            // activate all remaining hooks (in test class)
            activateFieldHooks(ownHookFields);
            tracker.hooksFromFields(ownHookFields, false, instance);
        }

        private void activateFieldHooks(final List<Field> fields) {
            HooksUtil.register(getFieldValues(fields));
        }

        @SuppressWarnings("unchecked")
        private <T> List<T> getFieldValues(final List<Field> fields) {
            return fields.isEmpty() ? Collections.emptyList() : (List<T>)
                    ReflectionUtils.readFieldValues(fields, instance);
        }

        private List<Field> findHookFields(final Class<?> testClass, final boolean includeInstanceFields) {
            List<Field> fields = AnnotationSupport.findAnnotatedFields(testClass, EnableHook.class);
            if (includeInstanceFields) {
                fields = new ArrayList<>(fields); // original list is unmodifiable
                // sort static fields first
                fields.sort(Comparator.comparing(field -> Modifier.isStatic(field.getModifiers()) ? 0 : 1));
            }
            HooksUtil.validateFieldHooks(fields, includeInstanceFields);
            return fields.isEmpty() ? Collections.emptyList() : new ArrayList<>(fields);
        }

        private List<Field> findSetupFields(final Class<?> testClass, final boolean includeInstanceFields) {
            final List<Field> fields = AnnotationSupport.findAnnotatedFields(testClass, EnableSetup.class);
            TestSetupUtils.validateFields(fields, includeInstanceFields);
            return fields.isEmpty() ? Collections.emptyList() : new ArrayList<>(fields);
        }
    }
}
