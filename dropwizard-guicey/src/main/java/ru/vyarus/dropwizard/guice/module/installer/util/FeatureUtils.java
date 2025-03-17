package ru.vyarus.dropwizard.guice.module.installer.util;

import ru.vyarus.dropwizard.guice.module.installer.FeatureInstaller;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

/**
 * Utility methods to simplify checks for feature installers.
 *
 * @author Vyacheslav Rusakov
 * @since 01.09.2014
 */
public final class FeatureUtils {

    private FeatureUtils() {
    }

    /**
     * @param type       type to check
     * @param annotation annotation to find
     * @return true if annotation found on class or super class and type is not abstract, false otherwise
     */
    public static boolean hasAnnotation(final Class<?> type, final Class<? extends Annotation> annotation) {
        return getAnnotation(type, annotation) != null;
    }

    /**
     * @param type       type to check
     * @param annotation annotation to find
     * @return true if annotation found on one of class or super class annotations and type is not abstract,
     * false otherwise
     */
    public static boolean hasAnnotatedAnnotation(final Class<?> type, final Class<? extends Annotation> annotation) {
        return getAnnotatedAnnotation(type, annotation) != null;
    }

    /**
     * @param type       type to examine
     * @param annotation annotation to search
     * @param <T>        annotation type
     * @return found annotation or null
     */
    public static <T extends Annotation> T getAnnotation(final Class<?> type, final Class<T> annotation) {
        T res = null;
        if (!Modifier.isAbstract(type.getModifiers())) {
            Class<?> supertype = type;
            while (supertype != null && Object.class != supertype) {
                if (supertype.isAnnotationPresent(annotation)) {
                    res = supertype.getAnnotation(annotation);
                    break;
                }
                supertype = supertype.getSuperclass();
            }
        }
        return res;
    }

    /**
     * @param type       type to examine
     * @param annotation annotation which must be found on target annotation
     * @param <T>        annotation type
     * @return annotation annotated with provided annotation type or null if not found
     */
    public static <T extends Annotation> Annotation getAnnotatedAnnotation(
            final Class<?> type, final Class<T> annotation) {
        Annotation res = null;
        if (!Modifier.isAbstract(type.getModifiers())) {
            Class<?> supertype = type;
            while (supertype != null && Object.class != supertype) {
                for (Annotation ann : supertype.getAnnotations()) {
                    if (ann.annotationType().isAnnotationPresent(annotation)) {
                        res = ann;
                        break;
                    }
                }
                if (res != null) {
                    break;
                }
                supertype = supertype.getSuperclass();
            }
        }
        return res;
    }

    /**
     * @param type     type to check
     * @param required required marker superclass or interface
     * @return if type implements interface or extends required type and type is not abstract
     */
    public static boolean is(final Class<?> type, final Class<?> required) {
        return !Modifier.isAbstract(type.getModifiers()) && required.isAssignableFrom(type);
    }

    /**
     * Utility call to prettify logs.
     *
     * @param installer installer class
     * @return installer name to use in logs
     */
    public static String getInstallerExtName(final Class<? extends FeatureInstaller> installer) {
        return installer.getSimpleName().replace("Installer", "").toLowerCase();
    }

    /**
     * @param type examining type
     * @param name method name
     * @param args method argument types
     * @return found method
     */
    @SafeVarargs
    public static Method findMethod(final Class<?> type, final String name, final Class<Object>... args) {
        try {
            return type.getMethod(name, args);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(String.format("Failed to obtain method '%s#%s(%s)' of class ",
                    type, name, Arrays.toString(args)), e);
        }
    }

    /**
     * @param method   method to call
     * @param instance object instance to call method on
     * @param args     optional arguments
     * @param <T>      expected type of execution result
     * @return method execution result
     */
    @SuppressWarnings("unchecked")
    public static <T> T invokeMethod(final Method method, final Object instance, final Object... args) {
        final boolean acc = method.canAccess(instance);
        method.setAccessible(true);
        try {
            return (T) method.invoke(instance, args);
        } catch (Exception e) {
            throw new IllegalStateException(String.format("Failed to invoke method '%s#%s(%s)'",
                    method.getDeclaringClass(), method.getName(), Arrays.toString(args)), e);
        } finally {
            method.setAccessible(acc);
        }
    }

    /**
     * Used to get correct object type even if it's guice proxy.
     *
     * @param object object to get class of
     * @param <T>    object type
     * @return object class
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<T> getInstanceClass(final T object) {
        final Class cls = object.getClass();
        return cls.getName().contains("$$EnhancerByGuice") ? (Class<T>) cls.getSuperclass() : cls;
    }
}
