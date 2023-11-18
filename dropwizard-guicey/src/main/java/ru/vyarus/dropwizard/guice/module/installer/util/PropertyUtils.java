package ru.vyarus.dropwizard.guice.module.installer.util;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * System properties utils. Used to read classes, configured in system properties.
 *
 * @author Vyacheslav Rusakov
 * @since 18.08.2019
 */
public final class PropertyUtils {

    private PropertyUtils() {
    }

    /**
     * Read system property value and instantiate configured classes. Classes are instantiated using
     * default constructor.
     *
     * @param name    system property name
     * @param aliases registered aliases
     * @param <T>     resulted instance type
     * @return list of resolved instance or empty list if nothing configured
     */
    public static <T> List<T> getProperty(final String name, final Map<String, String> aliases) {
        final String prop = System.getProperty(name);
        List<T> res = Collections.emptyList();
        if (prop != null) {
            final Iterable<String> classes = Splitter.on(',').omitEmptyStrings().trimResults().split(prop);
            try {
                res = toInstances(toClasses(classes, aliases));
            } catch (Exception e) {
                throw new IllegalStateException(String.format(
                        "Failed to parse system property '%s' value: '%s'", name, prop), e);
            }
        }
        return res;
    }

    /**
     * @param name  system property name
     * @param types classes to write to property
     */
    public static void setProperty(final String name, final Class<?>... types) {
        final String prop = Joiner.on(',').join(toStrings(Lists.newArrayList(types)));
        System.setProperty(name, prop);
    }


    private static List<String> toStrings(final Iterable<Class> list) {
        final List<String> res = Lists.newArrayList();
        for (Class cls : list) {
            res.add(cls.getName());
        }
        return res;
    }

    @SuppressWarnings("unchecked")
    private static <T> List<Class<T>> toClasses(final Iterable<String> list, final Map<String, String> aliases)
            throws Exception {
        final List<Class<T>> res = Lists.newArrayList();
        for (String cls : list) {
            res.add((Class<T>) Class.forName(aliases.getOrDefault(cls, cls)));
        }
        return res;
    }

    private static <T> List<T> toInstances(final Iterable<Class<T>> list) {
        final List<T> res = Lists.newArrayList();
        for (Class<T> cls : list) {
            res.add(InstanceUtils.create(cls));
        }
        return res;
    }
}
