package ru.vyarus.dropwizard.guice.test.jupiter.ext;

import com.google.common.base.Preconditions;
import io.dropwizard.testing.DropwizardTestSupport;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;
import ru.vyarus.dropwizard.guice.hook.ConfigurationHooksSupport;
import ru.vyarus.dropwizard.guice.injector.lookup.InjectorLookup;

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
 * For external integrations (other extensions), there is a special "hack" allowing to access
 * {@link DropwizardTestSupport} object (and so get access to injector): {@link #lookup(ExtensionContext)}.
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
    // indicator storage key of nested test (when extension activated in parent test)
    private static final String INHERITED_DW_SUPPORT = "INHERITED_DW_SUPPORT";

    @Override
    public void postProcessTestInstance(final Object testInstance, final ExtensionContext context) throws Exception {
        final DropwizardTestSupport<?> support = getSupport(context);
        InjectorLookup.getInjector(support.getApplication()).ifPresent(it -> it.injectMembers(testInstance));
    }

    @Override
    public void beforeAll(final ExtensionContext context) throws Exception {
        final ExtensionContext.Store store = getExtensionStore(context);
        if (store.get(DW_SUPPORT) == null) {
            final DropwizardTestSupport<?> support = prepareTestSupport(context);
            store.put(DW_SUPPORT, support);
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
    }

    /**
     * Static "hack" for other extensions extending base guicey extensions abilities.
     * <p>
     * The only thin moment here is extensions order! Junit preserve declaration order so in most cases it
     * should not be a problem.
     * <p>
     * To obtain guice injector use {@code InjectorLookup.getInjector(support.getApplication())}.
     *
     * @param extensionContext extension context
     * @return dropwizard support object prepared by guicey extension, or null if no guicey extension used or
     * its beforeAll hook was not called yet
     */
    public static Optional<DropwizardTestSupport<?>> lookup(final ExtensionContext extensionContext) {
        return Optional.ofNullable((DropwizardTestSupport<?>) getExtensionStore(extensionContext).get(DW_SUPPORT));
    }

    /**
     * The only role of actual extension class is to configure {@link DropwizardTestSupport} object
     * according to annotated configuration.
     *
     * @param context extension context
     * @return configured dropwizard test support object
     */
    protected abstract DropwizardTestSupport<?> prepareTestSupport(ExtensionContext context);

    @Override
    protected DropwizardTestSupport<?> getSupport(final ExtensionContext extensionContext) {
        return lookup(extensionContext).orElse(null);
    }

    private static ExtensionContext.Store getExtensionStore(final ExtensionContext context) {
        // Store is extension specific, but nested tests will see it too (because key is extension class)
        return context.getStore(ExtensionContext.Namespace
                .create(GuiceyExtensionsSupport.class));
    }

    private ExtensionContext.Store getLocalExtensionStore(final ExtensionContext context) {
        // test scoped extension scope (required to differentiate nested classes or parameterized executions)
        return context.getStore(ExtensionContext.Namespace
                .create(GuiceyExtensionsSupport.class, context.getRequiredTestClass()));
    }
}
