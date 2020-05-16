package ru.vyarus.dropwizard.guice.test.jupiter.ext;

import com.google.common.base.Preconditions;
import com.google.inject.Injector;
import io.dropwizard.testing.DropwizardTestSupport;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;
import org.junit.platform.commons.support.HierarchyTraversalMode;
import org.junit.platform.commons.support.ReflectionSupport;
import org.junit.platform.commons.util.ReflectionUtils;
import ru.vyarus.dropwizard.guice.hook.ConfigurationHooksSupport;
import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook;
import ru.vyarus.dropwizard.guice.injector.lookup.InjectorLookup;
import ru.vyarus.dropwizard.guice.test.jupiter.param.ClientSupport;
import ru.vyarus.dropwizard.guice.test.util.HooksUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Optional;

/**
 * Base class for junit 5 extensions implementations. All extensions use {@link DropwizardTestSupport} object
 * for actual execution (only configuration differs).
 * <p>
 * Extensions assumed to be used only on class level: extension will start dropwizard app before all tests
 * and shut down it after all tests. If nested tests used - they also affected. Execution per test is not allowed
 * because these tests are integration tests and they must minimize environment preparation time. Group tests
 * not affecting application state into one class and use different test classes (or nested classes) for tests
 * modifying state.
 * <p>
 * Test instance is not managed by guice! Only {@link com.google.inject.Injector#injectMembers(Object)} applied
 * for it to process test fields injection. Guice AOP can't be used on test methods. Technically, creating test
 * instances with guice is possible, but in this case nested tests could not work at all, which is unacceptable.
 * <p>
 * Extension detects static fields of {@link GuiceyConfigurationHook} type and initialize these hooks automatically.
 * It was done like this to simplify customizations, when main extension could be declared as annotation and
 * hook as field. Also, it was impossible additional to implement hooks support with junit extension. Hook
 * field could be declared even in base test class.
 * <p>
 * For external integrations (other extensions), there is a special "hack" allowing to access
 * {@link DropwizardTestSupport} object (and so get access to injector): {@link #lookupSupport(ExtensionContext)}.
 * And shortcuts {@link #lookupInjector(ExtensionContext)} and {@link #lookupClient(ExtensionContext)}.
 *
 * @author Vyacheslav Rusakov
 * @see TestParametersSupport for supported test parameters
 * @since 29.04.2020
 */
public abstract class GuiceyExtensionsSupport extends TestParametersSupport implements TestInstancePostProcessor,
        BeforeAllCallback,
        AfterAllCallback {

    // dropwizard support storage key (store visible for all relative tests)
    private static final String DW_SUPPORT = "DW_SUPPORT";
    // ClientFactory instance
    private static final String DW_CLIENT = "DW_CLIENT";
    // indicator storage key of nested test (when extension activated in parent test)
    private static final String INHERITED_DW_SUPPORT = "INHERITED_DW_SUPPORT";

    @Override
    public void postProcessTestInstance(final Object testInstance, final ExtensionContext context) throws Exception {
        final DropwizardTestSupport<?> support = Preconditions.checkNotNull(getSupport(context));
        final Optional<Injector> injector = InjectorLookup.getInjector(support.getApplication());
        Preconditions.checkState(injector.isPresent(),
                "Can't find guicey injector to process test fields injections");
        injector.get().injectMembers(testInstance);
    }

    @Override
    public void beforeAll(final ExtensionContext context) throws Exception {
        final ExtensionContext.Store store = getExtensionStore(context);
        if (store.get(DW_SUPPORT) == null) {
            final DropwizardTestSupport<?> support = prepareTestSupport(context);
            store.put(DW_SUPPORT, support);
            // for pure guicey tests client may seem redundant, but it can be used for calling other services
            store.put(DW_CLIENT, new ClientSupport(support));

            // find and activate hooks declared in test static fields (impossible to do with an extension)
            activateFieldHooks(context.getRequiredTestClass());

            support.before();
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
    public void afterAll(final ExtensionContext context) throws Exception {
        // just in case, normally hooks cleared automatically after appliance
        ConfigurationHooksSupport.reset();

        final ExtensionContext.Store localExtensionStore = getLocalExtensionStore(context);
        if (localExtensionStore.get(INHERITED_DW_SUPPORT) != null) {
            localExtensionStore.remove(INHERITED_DW_SUPPORT);
            // do nothing: extension managed on upper context
            return;
        }

        final DropwizardTestSupport<?> support = getSupport(context);
        if (support != null) {
            support.after();
        }
        final ClientSupport client = getClient(context);
        if (client != null) {
            client.close();
        }
        onShutdown(context);
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
     * according to annotated configuration.
     *
     * @param context extension context
     * @return configured dropwizard test support object
     */
    protected abstract DropwizardTestSupport<?> prepareTestSupport(ExtensionContext context);

    /**
     * Hook to perform additional work after server shutdown. Useful for custom commands in order to shutdown properly.
     *
     * @param context test context
     */
    protected void onShutdown(final ExtensionContext context) {
    }

    @Override
    protected DropwizardTestSupport<?> getSupport(final ExtensionContext extensionContext) {
        return lookupSupport(extensionContext).orElse(null);
    }

    @Override
    protected ClientSupport getClient(final ExtensionContext extensionContext) {
        // throw exception when used improperly (to avoid null parameter injection)
        return lookupClient(extensionContext).get();
    }

    @Override
    protected Optional<Injector> getInjector(final ExtensionContext extensionContext) {
        return lookupInjector(extensionContext);
    }

    protected static ExtensionContext.Store getExtensionStore(final ExtensionContext context) {
        // Store is extension specific, but nested tests will see it too (because key is extension class)
        return context.getStore(ExtensionContext.Namespace
                .create(GuiceyExtensionsSupport.class));
    }

    @SuppressWarnings({"unchecked", "checkstyle:Indentation"})
    private void activateFieldHooks(final Class<?> testClass) {
        final List<Field> fields = ReflectionSupport.findFields(testClass,
                field -> Modifier.isStatic(field.getModifiers())
                        && GuiceyConfigurationHook.class.isAssignableFrom(field.getType()),
                HierarchyTraversalMode.BOTTOM_UP);
        if (!fields.isEmpty()) {
            HooksUtil.register((List<GuiceyConfigurationHook>)
                    (List) ReflectionUtils.readFieldValues(fields, null));
        }
    }

    private ExtensionContext.Store getLocalExtensionStore(final ExtensionContext context) {
        // test scoped extension scope (required to differentiate nested classes or parameterized executions)
        return context.getStore(ExtensionContext.Namespace
                .create(GuiceyExtensionsSupport.class, context.getRequiredTestClass()));
    }
}
