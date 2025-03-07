package ru.vyarus.dropwizard.guice.test.jupiter.ext.conf.track;

import ru.vyarus.dropwizard.guice.debug.util.RenderUtils;
import ru.vyarus.dropwizard.guice.module.installer.util.StackUtils;
import ru.vyarus.dropwizard.guice.test.jupiter.env.field.AnnotatedField;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/**
 * Utilities for rendering registration tracking info for hooks and setup test objects.
 *
 * @author Vyacheslav Rusakov
 * @since 20.05.2022
 */
@SuppressWarnings("PMD.UseVarargs")
public final class RegistrationTrackUtils {

    private static final String AUTO_RECOGNITION = "at r.v.d.g.test.util.(TestSetupUtils.java:108)";

    private RegistrationTrackUtils() {
    }

    /**
     * Stores tracking info for registered classes.
     *
     * @param info    info holder
     * @param prefix  source identity
     * @param classes items to append
     */
    public static void fromClass(final List<String> info, final String prefix, final Class<?>[] classes,
                                 final boolean fromAnnotation) {
        // it is not possible to detect the source file by annotation here
        final String src = fromAnnotation ? prefix : buildSourceLocation(prefix);
        track(info, Arrays.asList(classes), it -> it, it -> src);
    }

    /**
     * Stores tracking info for registered instances.
     *
     * @param info      info holder
     * @param prefix    source identity
     * @param instances instances to append
     */
    public static void fromInstance(final List<String> info, final String prefix, final Object[] instances) {
        final String src = buildSourceLocation(prefix);
        track(info, Arrays.asList(instances), Object::getClass, obj -> src);
    }

    /**
     * Store tracking info for recognized test class fields.
     *
     * @param info     info holder
     * @param prefix   source identity
     * @param fields   fields to append
     * @param instance test instance or null for static fields
     */
    public static void fromField(final List<String> info,
                                 final String prefix,
                                 final List<AnnotatedField<?, ?>> fields,
                                 final Object instance) {
        track(info, fields,
                field -> field.getValue(instance).getClass(),
                field -> formatSourceLocation(prefix + " " + getFieldDescriptor(field),
                        "at " + RenderUtils.renderClass(field.getDeclaringClass()) + "#" + field.getName()
                ));
    }

    /**
     * @param field field
     * @return field description string
     */
    public static String getFieldDescriptor(final AnnotatedField<?, ?> field) {
        return RenderUtils.getClassName(field.getDeclaringClass()) + "#" + field.getName();
    }

    private static String buildSourceLocation(final String prefix) {
        final String source = StackUtils.getTestExtensionSource();
        return formatSourceLocation(
                (AUTO_RECOGNITION.equals(source) ? "auto recognition" : prefix), source);
    }

    private static String formatSourceLocation(final String prefix, final String src) {
        return String.format("%-50s %s", prefix, src);
    }

    private static <T> void track(final List<String> info,
                                  final List<T> objects,
                                  final Function<T, Class> converter,
                                  final Function<T, String> marker) {
        for (T obj : objects) {
            final Class<?> cls = converter.apply(obj);
            final String className;
            // avoid showing ugly not informative class names for anonymous and lambda classes
            if (cls.isAnonymousClass() || cls.isSynthetic()) {
                className = "<lambda>";
            } else {
                className = RenderUtils.getClassName(cls);
            }
            info.add(String.format("%-30s \t%s", className, marker.apply(obj)));
        }
    }
}
