package ru.vyarus.dropwizard.guice.module.installer.util;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import ru.vyarus.dropwizard.guice.module.context.ConfigurationContext;
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBootstrap;
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle;

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
public final class BundleSupport {

    private BundleSupport() {
    }

    /**
     * Process initially registered and all transitive bundles.
     * <ul>
     * <li>Executing initial bundles (registered in {@link ru.vyarus.dropwizard.guice.GuiceBundle}
     * and by bundle lookup)</li>
     * <li>During execution bundles may register other bundles (through {@link GuiceyBootstrap})</li>
     * <li>Execute registered bundles and repeat from previous step until no new bundles registered</li>
     * </ul>
     * Bundles duplicates are checked by type: only one bundle instance may be registered.
     *
     * @param context       bundles context
     * @param configuration configuration object
     * @param environment   environment object
     */
    public static void processBundles(final ConfigurationContext context,
                                      final Configuration configuration, final Environment environment) {
        final List<GuiceyBundle> bundles = context.getBundles();
        final List<Class<? extends GuiceyBundle>> installedBundles = Lists.newArrayList();
        final GuiceyBootstrap guiceyBootstrap = new GuiceyBootstrap(context, bundles, configuration, environment);

        // iterating while no new bundles registered
        while (!bundles.isEmpty()) {
            final List<GuiceyBundle> processingBundles = Lists.newArrayList(BundleSupport.removeDuplicates(bundles));
            bundles.clear();
            for (GuiceyBundle bundle : removeTypes(processingBundles, installedBundles)) {

                final Class<? extends GuiceyBundle> bundleType = bundle.getClass();
                Preconditions.checkState(!installedBundles.contains(bundleType),
                        "State error: duplicate bundle '%s' registration", bundleType.getName());

                context.setScope(bundleType);
                bundle.initialize(guiceyBootstrap);
                installedBundles.add(bundleType);
                context.closeScope();
            }
        }
    }

    /**
     * Remove duplicates in list by rule: only one instance of type must be present in list.
     *
     * @param list bundles list
     * @param <T>  required bundle type
     * @return list cleared from duplicates
     */
    public static <T> List<T> removeDuplicates(final List<T> list) {
        final List<Class> registered = Lists.newArrayList();
        final Iterator it = list.iterator();
        while (it.hasNext()) {
            final Class type = it.next().getClass();
            if (registered.contains(type)) {
                it.remove();
            } else {
                registered.add(type);
            }
        }
        return list;
    }

    /**
     * Filter list from objects of type present in filter list.
     *
     * @param list   list to filter
     * @param filter types to filter
     * @param <T>    required type
     * @return filtered list
     */
    public static <T> List<T> removeTypes(final List<T> list, final List<Class<? extends T>> filter) {
        final Iterator it = list.iterator();
        while (it.hasNext()) {
            final Class type = it.next().getClass();
            if (filter.contains(type)) {
                it.remove();
            }
        }
        return list;
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
