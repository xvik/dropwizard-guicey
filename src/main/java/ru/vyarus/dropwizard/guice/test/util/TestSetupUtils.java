package ru.vyarus.dropwizard.guice.test.util;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.support.AnnotationSupport;
import org.junit.platform.commons.util.ReflectionUtils;
import ru.vyarus.dropwizard.guice.test.jupiter.env.EnableSetup;
import ru.vyarus.dropwizard.guice.test.jupiter.env.TestEnvironmentSetup;
import ru.vyarus.dropwizard.guice.test.jupiter.env.TestExtension;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.conf.ExtensionConfig;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
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
     * Search for fields annotated with {@link ru.vyarus.dropwizard.guice.test.jupiter.env.EnableSetup}, validate
     * them, and execute all configured setup objects.
     *
     * @param config  extension configuration
     * @param context junit extension context
     */
    public static void findAndProcessSetupObjects(final ExtensionConfig config, final ExtensionContext context) {
        final List<TestEnvironmentSetup> setups = analyzeFields(context.getRequiredTestClass());
        if (!setups.isEmpty()) {
            if (config.extensions == null) {
                config.extensions = setups;
            } else {
                config.extensions.addAll(setups);
            }
        }
        executeSetup(config, context);
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
                res.add(ext.newInstance());
            } catch (Exception e) {
                throw new IllegalStateException("Failed to instantiate test support object: " + ext.getSimpleName(), e);
            }
        }
        return res;
    }

    /**
     * Search for fields annotated with {@link ru.vyarus.dropwizard.guice.test.jupiter.env.EnableSetup} in
     * test class and collect support objects.
     *
     * @param testClass test class
     * @return resolved support objects or empty list
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static List<TestEnvironmentSetup> analyzeFields(final Class<?> testClass) {
        final List<Field> fields = AnnotationSupport.findAnnotatedFields(testClass, EnableSetup.class);
        validateFields(fields);
        if (fields.isEmpty()) {
            return Collections.emptyList();
        }
        return (List<TestEnvironmentSetup>) (List) ReflectionUtils.readFieldValues(fields, null);
    }

    /**
     * Execute all configured support objects. If object returns closable then register it in junit storage
     * for automatic closing.
     *
     * @param config  extension config
     * @param context junit extension context
     */
    private static void executeSetup(final ExtensionConfig config, final ExtensionContext context) {
        if (config.extensions != null) {
            final TestExtension builder = new TestExtension(config);
            final ExtensionContext.Store store = context.getStore(
                    ExtensionContext.Namespace.create(TestEnvironmentSetup.class));
            for (TestEnvironmentSetup support : config.extensions) {
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
     * @param fields fields to validate
     */
    private static void validateFields(final List<Field> fields) {
        for (Field field : fields) {
            if (!TestEnvironmentSetup.class.isAssignableFrom(field.getType())) {
                throw new IllegalStateException(String.format(
                        "Field %s annotated with @%s, but its type is not %s",
                        toString(field), EnableSetup.class.getSimpleName(),
                        TestEnvironmentSetup.class.getSimpleName()
                ));
            }
            if (!Modifier.isStatic(field.getModifiers())) {
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
