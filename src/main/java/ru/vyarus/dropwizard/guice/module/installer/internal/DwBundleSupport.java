package ru.vyarus.dropwizard.guice.module.installer.internal;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import io.dropwizard.setup.Bootstrap;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Utility class to work with registered {@link io.dropwizard.Bundle} and {@link io.dropwizard.ConfiguredBundle}
 * objects within dropwizard {@link Bootstrap} object.
 *
 * @author Vyacheslav Rusakov
 * @since 01.08.2015
 */
public final class DwBundleSupport {

    private DwBundleSupport() {
    }

    /**
     * @param bootstrap dropwizard bootstrap instance
     * @param type      required bundle type (or marker interface)
     * @param <T>       required bundle type
     * @return list of bundles of specified type
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> findBundles(final Bootstrap bootstrap, final Class<T> type) {
        final List bundles = Lists.newArrayList(resolveBundles(bootstrap, "bundles"));
        bundles.addAll(resolveBundles(bootstrap, "configuredBundles"));
        final Iterator it = bundles.iterator();
        while (it.hasNext()) {
            if (!type.isAssignableFrom(it.next().getClass())) {
                it.remove();
            }
        }
        return bundles;
    }

    @SuppressWarnings("unchecked")
    private static <T> List<T> resolveBundles(final Bootstrap bootstrap, final String field) {
        try {
            final Field declaredField = Bootstrap.class.getDeclaredField(field);
            declaredField.setAccessible(true);
            final List<T> res = (List<T>) declaredField.get(bootstrap);
            declaredField.setAccessible(false);
            // in case of mock bootstrap (tests)
            return MoreObjects.firstNonNull(res, Collections.<T>emptyList());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to resolve bootstrap filed " + field, e);
        }
    }
}
