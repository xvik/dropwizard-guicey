package ru.vyarus.dropwizard.guice.test.util;

import com.google.common.base.Preconditions;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.platform.commons.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.GuiceyExtensionsSupport;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.conf.ExtensionConfig;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;

/**
 * Reusable application support for tests. Reusable application must be declared in base test class (preferably
 * abstract) to guarantee that all derived tests would use THE SAME extension declaration.
 * <p>
 * Reusable application stored in root context under spacial key: base test class name. DW_SUPPORT key can't be used
 * because root storage is visible by all tests, but only some of them could extend base class and so should
 * actually use global application. Moreover, there might be multiple reusable applications used (when multiple
 * base classes used with reusable application declaration).
 * <p>
 * Global application would be started by the first test and shut down after all tests execution (automatically
 * on storage close).
 * <p>
 * There is an additional api for extensions to be able to close reusable application:
 * {@link ru.vyarus.dropwizard.guice.test.jupiter.ext.GuiceyExtensionsSupport#closeReusableApp(
 * org.junit.jupiter.api.extension.ExtensionContext, Class)}.
 *
 * @author Vyacheslav Rusakov
 * @since 19.12.2022
 */
@SuppressWarnings("PMD.AvoidSynchronizedAtMethodLevel")
public final class ReusableAppUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReusableAppUtils.class);

    private ReusableAppUtils() {
    }

    /**
     * Search for exact class in test class clas hierarchy where annotation was declared.
     *
     * @param test test class
     * @param ann  annotation to find
     * @return found annotation
     * @throws java.lang.NullPointerException  if annotation not found
     * @throws java.lang.IllegalStateException if declaration is not correct
     */
    public static Class<?> findDeclarationClass(final Class<?> test, final Annotation ann) {
        Class<?> sup = test;
        final Class<? extends Annotation> annType = ann.annotationType();
        Class<?> decl = null;
        while (decl == null && sup != null && !Object.class.equals(sup)) {
            final Annotation cand = sup.getDeclaredAnnotation(annType);
            if (cand != null && cand.equals(ann)) {
                decl = sup;
            }
            sup = sup.getSuperclass();
        }
        Preconditions.checkNotNull(decl, "Failed to find declaration class for @%s in test class %s hierarchy",
                ann.annotationType().getSimpleName(), test.getName());
        validateDeclaringClass(test, decl, test.getName() + " (@" + annType.getSimpleName() + ")");
        return decl;
    }

    /**
     * Search for field in test class hierarchy where extension was declared.
     * <p>
     * Field would not be found if extension declared in non-static field.
     *
     * @param test test class
     * @param ext  extension instance
     * @return field declared extension
     * @throws java.lang.NullPointerException  if field not found
     * @throws java.lang.IllegalStateException if declaration is not correct
     */
    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH")
    public static Field findExtensionField(final Class<?> test, final GuiceyExtensionsSupport ext) {
        final List<Field> fields = ReflectionUtils.findFields(test,
                field -> Modifier.isStatic(field.getModifiers()) && field.isAnnotationPresent(RegisterExtension.class),
                ReflectionUtils.HierarchyTraversalMode.BOTTOM_UP);
        Field target = null;
        for (Field field : fields) {
            try {
                final Object value = ReflectionUtils.tryToReadFieldValue(field).get();
                if (value != null && value.equals(ext)) {
                    target = field;
                    break;
                }
            } catch (Exception ex) {
                throw new IllegalStateException("Failed to read field value: " + field.getName(), ex);
            }
        }
        Preconditions.checkNotNull(target, "Failed to find declaration field for %s extension in test "
                        + "class %s hierarchy. Probably, reusable app declared in non-static field.",
                ext.getClass().getSimpleName(), test.getName());
        final Class<?> declaringClass = target.getDeclaringClass();
        validateDeclaringClass(test, declaringClass, declaringClass.getName() + "." + target.getName());
        return target;
    }

    /**
     * Register reusable app declaration source.
     *
     * @param test   test class
     * @param ext    extension instance
     * @param config extension config
     */
    public static void registerField(final Class<?> test,
                                     final GuiceyExtensionsSupport ext,
                                     final ExtensionConfig config) {
        final Field field = findExtensionField(test, ext);
        config.reuseDeclarationClass = field.getDeclaringClass();
        config.reuseSource = field.getDeclaringClass().getName() + "." + field.getName();
    }

    /**
     * Register reusable app declaration source.
     *
     * @param test   test class
     * @param ann    extension annotation
     * @param config extension config
     */
    public static void registerAnnotation(final Class<?> test,
                                          final Annotation ann,
                                          final ExtensionConfig config) {
        final Class<?> declare = findDeclarationClass(test, ann);
        config.reuseDeclarationClass = declare;
        config.reuseSource = declare.getName() + "@" + ann.annotationType().getSimpleName();
    }

    /**
     * @param context any context
     * @return global store where reusable application must be stored
     */
    public static ExtensionContext.Store getGlobalStore(final ExtensionContext context) {
        return context.getRoot().getStore(ExtensionContext.Namespace.create(GuiceyExtensionsSupport.class));
    }

    /**
     * @param context          any context
     * @param declarationClass base test class where reusable app was declared
     * @return reusable app holder or null
     */
    public static synchronized StoredReusableApp getGlobalApp(
            final ExtensionContext context, final Class<?> declarationClass) {
        final ExtensionContext.Store global = getGlobalStore(context);
        return (StoredReusableApp) global.get(getKey(declarationClass));
    }

    /**
     * Stores new application in global context.
     *
     * @param context any context
     * @param app application holder to store
     */
    public static synchronized void registerGlobalApp(final ExtensionContext context, final StoredReusableApp app) {
        final ExtensionContext.Store globalStore = getGlobalStore(context);
        final String key = getKey(app.getDeclaration());
        // just in case
        if (globalStore.get(key) != null) {
            throw new IllegalStateException(String.format("Can't register reusable application %s because "
                            + "another application %s is already registered for base class %s", app.getSource(),
                    ((StoredReusableApp) globalStore.get(key)).getSource(), app.getDeclaration().getName()));
        }
        globalStore.put(key, app);
    }

    /**
     * Do nothing if reusable application not found for provided base class.
     *
     * @param context any context
     * @param declarationClass base test class where reusable app was declared
     */
    public static synchronized void closeGlobalApp(final ExtensionContext context, final Class<?> declarationClass) {
        final StoredReusableApp app = getGlobalApp(context, declarationClass);
        if (app != null) {
            LOGGER.warn("Requesting manual close for reusable app {}", app.getSource());
            try {
                app.close();
            } catch (Exception e) {
                LOGGER.error("Error closing reusable app manually", e);
            }
            getGlobalStore(context).remove(getKey(declarationClass));
        }
    }

    private static void validateDeclaringClass(final Class<?> test, final Class<?> declaration, final String source) {
        Preconditions.checkState(!test.equals(declaration),
                "Application declared in %s can't be reused because reusable declaration must be in abstract (base) "
                        + "class so tests could share the same declaration", source);
    }

    private static String getKey(final Class<?> declarationClass) {
        return declarationClass.getName();
    }
}
