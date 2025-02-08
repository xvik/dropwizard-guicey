package ru.vyarus.dropwizard.guice.test.util;

import ru.vyarus.dropwizard.guice.debug.util.RenderUtils;

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

    private RegistrationTrackUtils() {
    }

    /**
     * Stores tracking info for registered classes.
     *
     * @param info    info holder
     * @param prefix  source identity
     * @param classes items to append
     */
    public static void fromClass(final List<String> info, final String prefix, final Class<?>[] classes) {
        track(info, Arrays.asList(classes), it -> it, it -> prefix);
    }

    /**
     * Stores tracking info for registered instances.
     *
     * @param info      info holder
     * @param prefix    source identity
     * @param instances instances to append
     */
    public static void fromInstance(final List<String> info, final String prefix, final Object[] instances) {
        track(info, Arrays.asList(instances), Object::getClass, obj -> prefix);
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
                                 final List<FieldAccess<?, ?>> fields,
                                 final Object instance) {
        track(info, fields,
                field -> field.getValue(instance).getClass(),
                field -> prefix + " field " + field.getDeclaringClass().getSimpleName()
                        + "." + field.getName()
        );
    }

    private static <T> void track(final List<String> info,
                                  final List<T> objects,
                                  final Function<T, Class> converter,
                                  final Function<T, String> marker) {
        for (T obj : objects) {
            final Class<?> cls = converter.apply(obj);
            info.add(String.format("%-80s \t%s", RenderUtils.renderClassLine(cls), marker.apply(obj)));
        }
    }
}
