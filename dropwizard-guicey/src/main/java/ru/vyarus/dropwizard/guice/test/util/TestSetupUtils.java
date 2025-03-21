package ru.vyarus.dropwizard.guice.test.util;

import com.google.common.base.Stopwatch;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.extension.ExtensionContext;
import ru.vyarus.dropwizard.guice.module.installer.util.InstanceUtils;
import ru.vyarus.dropwizard.guice.test.jupiter.env.ListenersSupport;
import ru.vyarus.dropwizard.guice.test.jupiter.env.TestEnvironmentSetup;
import ru.vyarus.dropwizard.guice.test.jupiter.env.TestExtension;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.conf.ExtensionConfig;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.conf.track.GuiceyTestTime;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * Guicey {@link ru.vyarus.dropwizard.guice.test.jupiter.env.TestEnvironmentSetup} test utilities.
 *
 * @author Vyacheslav Rusakov
 * @since 13.05.2022
 */
public final class TestSetupUtils {

    private TestSetupUtils() {
    }

    /**
     * @return environment setup objects from service lookup
     */
    public static TestEnvironmentSetup[] lookup() {
        return Lists.newArrayList(ServiceLoader.load(TestEnvironmentSetup.class))
                .toArray(TestEnvironmentSetup[]::new);
    }

    /**
     * Instantiates provided support objects.
     *
     * @param extensions extension classes to instantiate
     * @return extension instances
     */
    @SafeVarargs
    public static List<TestEnvironmentSetup> create(final Class<? extends TestEnvironmentSetup>... extensions) {
        final List<TestEnvironmentSetup> res = new ArrayList<>();
        for (Class<? extends TestEnvironmentSetup> ext : extensions) {
            try {
                res.add(InstanceUtils.create(ext));
            } catch (Exception e) {
                throw new IllegalStateException("Failed to instantiate test support object: " + ext.getSimpleName(), e);
            }
        }
        return res;
    }

    /**
     * Execute all configured support objects. If object returns closable then register it in junit storage
     * for automatic closing.
     *
     * @param config  extension config
     * @param context junit extension context
     */
    public static void executeSetup(final ExtensionConfig config,
                                    final ExtensionContext context,
                                    final ListenersSupport listeners) {
        if (!config.extensions.isEmpty()) {
            final Stopwatch timer = Stopwatch.createStarted();
            final TestExtension builder = new TestExtension(config, context, listeners);
            final ExtensionContext.Store store = context.getStore(
                    ExtensionContext.Namespace.create(TestEnvironmentSetup.class));
            for (TestEnvironmentSetup support : config.extensions) {
                final Object res = setup(support, config, builder);
                // method could return anything, but only closable object would be tracked
                if (res instanceof AutoCloseable || res instanceof ExtensionContext.Store.CloseableResource) {
                    // would be closed automatically with storage closing
                    store.put(support.getClass(), ClosableWrapper.create(res));
                }
            }
            config.tracker.performanceTrack(GuiceyTestTime.SetupObjectsExecution, timer.elapsed());
        }
    }

    /**
     * Use display names, so would correctly show spock test name or junit test with @DisplayName.
     *
     * @param context junit context
     * @return context string (test name of beforeAll and test + method for beforeEach)
     */
    public static String getContextTestName(final ExtensionContext context) {
        // display name will show the correct name in case of groovy test (or will show @DisplayName value)
        String res = context.getDisplayName();
        if (context.getTestMethod().isPresent()) {
            res = context.getParent().get().getDisplayName() + '#' + res;
        }
        return res;
    }

    private static Object setup(final TestEnvironmentSetup support,
                                final ExtensionConfig config,
                                final TestExtension builder) {
        // required to recognize hooks registered from setup objects
        config.tracker.setContextSetupObject(support.getClass());
        try {
            final Object res = support.setup(builder);
            config.tracker.setContextSetupObject(null);
            return res;
        } catch (Exception ex) {
            Throwables.throwIfUnchecked(ex);
            throw new IllegalStateException("Failed to run test setup object", ex);
        }
    }

    /**
     * Wrapper class for {@link java.lang.AutoCloseable} objects to grant automatic closing inside junit store.
     */
    public static class ClosableWrapper implements ExtensionContext.Store.CloseableResource {
        private final AutoCloseable obj;

        public ClosableWrapper(final AutoCloseable obj) {
            this.obj = obj;
        }

        @Override
        public void close() throws Throwable {
            obj.close();
        }

        /**
         * @param obj closable object
         * @return either object as is if it is already closable resource or wrapped object
         */
        public static ExtensionContext.Store.CloseableResource create(final Object obj) {
            return obj instanceof ExtensionContext.Store.CloseableResource
                    ? (ExtensionContext.Store.CloseableResource) obj
                    : new ClosableWrapper((AutoCloseable) obj);
        }
    }
}
