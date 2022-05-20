package ru.vyarus.dropwizard.guice.test.util;

import org.apache.commons.lang3.StringUtils;
import ru.vyarus.dropwizard.guice.debug.util.RenderUtils;

import java.lang.reflect.Field;
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
        track(info, prefix, Arrays.asList(classes), RenderUtils::renderClass);
    }

    /**
     * Stores tracking info for registered instances.
     *
     * @param info      info holder
     * @param prefix    source identity
     * @param instances instances to append
     */
    public static void fromInstance(final List<String> info, final String prefix, final Object[] instances) {
        track(info, prefix, Arrays.asList(instances), obj -> RenderUtils.renderClass(obj.getClass()));
    }

    /**
     * Stores tracking info for recognized test class fields.
     *
     * @param info   info holder
     * @param prefix source identity
     * @param fields fields to append
     */
    public static void fromField(final List<String> info, final String prefix, final List<Field> fields) {
        track(info, prefix, fields,
                field -> RenderUtils.renderClass(field.getDeclaringClass()) + "." + field.getName());
    }

    private static <T> void track(final List<String> info,
                                  final String prefix,
                                  final List<T> objects,
                                  final Function<T, String> transformer) {
        int i = 0;
        final String blank = StringUtils.repeat(' ', prefix.length());
        for (T obj : objects) {
            info.add((i++ == 0 ? prefix : blank) + " " + transformer.apply(obj));
        }
    }
}
