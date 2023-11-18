package ru.vyarus.dropwizard.guice.test.util;

import org.junit.jupiter.api.extension.ExtensionContext;
import ru.vyarus.dropwizard.guice.module.installer.util.InstanceUtils;
import ru.vyarus.dropwizard.guice.test.jupiter.env.EnableSetup;
import ru.vyarus.dropwizard.guice.test.jupiter.env.TestEnvironmentSetup;
import ru.vyarus.dropwizard.guice.test.jupiter.env.TestExtension;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.conf.ExtensionConfig;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

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
    public static void executeSetup(final ExtensionConfig config, final ExtensionContext context) {
        if (!config.extensions.isEmpty()) {
            final TestExtension builder = new TestExtension(config);
            final ExtensionContext.Store store = context.getStore(
                    ExtensionContext.Namespace.create(TestEnvironmentSetup.class));
            for (TestEnvironmentSetup support : config.extensions) {
                // required to recognize hooks registered from setup objects
                config.tracker.setContextHook(support.getClass());
                final Object res = support.setup(builder);
                // method could return anything, but only closable object would be tracked
                if (res instanceof AutoCloseable || res instanceof ExtensionContext.Store.CloseableResource) {
                    // would be closed automatically with storage closing
                    store.put(support.getClass(), ClosableWrapper.create(res));
                }
            }
        }
    }

    /**
     * Validate fields annotated with {@link ru.vyarus.dropwizard.guice.test.jupiter.env.EnableSetup}
     * for correctness.
     *
     * @param fields                fields to validate
     * @param includeInstanceFields true to allow instance fields, false to break if instance field detected
     */
    public static void validateFields(final List<Field> fields, final boolean includeInstanceFields) {
        for (Field field : fields) {
            if (!TestEnvironmentSetup.class.isAssignableFrom(field.getType())) {
                throw new IllegalStateException(String.format(
                        "Field %s annotated with @%s, but its type is not %s",
                        toString(field), EnableSetup.class.getSimpleName(),
                        TestEnvironmentSetup.class.getSimpleName()
                ));
            }
            if (!includeInstanceFields && !Modifier.isStatic(field.getModifiers())) {
                throw new IllegalStateException(String.format("Field %s annotated with @%s must be static",
                        toString(field), EnableSetup.class.getSimpleName()));
            }
        }
    }

    private static String toString(final Field field) {
        return field.getDeclaringClass().getName() + "." + field.getName();
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
