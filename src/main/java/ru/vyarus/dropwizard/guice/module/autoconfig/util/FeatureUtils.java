package ru.vyarus.dropwizard.guice.module.autoconfig.util;

import ru.vyarus.dropwizard.guice.module.autoconfig.feature.FeatureInstaller;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;

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
        boolean has = false;
        if (!Modifier.isAbstract(type.getModifiers())) {
            Class<?> supertype = type;
            while (supertype != null && Object.class != supertype) {
                if (type.isAnnotationPresent(annotation)) {
                    has = true;
                    break;
                }
                supertype = supertype.getSuperclass();
            }
        }
        return has;
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
}
