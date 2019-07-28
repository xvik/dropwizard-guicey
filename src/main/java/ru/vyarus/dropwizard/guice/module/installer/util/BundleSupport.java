package ru.vyarus.dropwizard.guice.module.installer.util;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import io.dropwizard.setup.Bootstrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vyarus.dropwizard.guice.module.context.ConfigurationContext;
import ru.vyarus.dropwizard.guice.module.context.info.ItemId;
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBootstrap;
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle;
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyEnvironment;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class to work with registered {@link io.dropwizard.ConfiguredBundle} objects within dropwizard
 * {@link Bootstrap} object.
 *
 * @author Vyacheslav Rusakov
 * @since 01.08.2015
 */
public final class BundleSupport {
    private static final Logger LOGGER = LoggerFactory.getLogger(BundleSupport.class);

    private BundleSupport() {
    }

    /**
     * Process initialization for initially registered and all transitive bundles.
     * <ul>
     * <li>Executing initial bundles initialization (registered in {@link ru.vyarus.dropwizard.guice.GuiceBundle}
     * and by bundle lookup)</li>
     * <li>During execution bundles may register other bundles (through {@link GuiceyBootstrap})</li>
     * <li>Execute registered bundles and repeat from previous step until no new bundles registered</li>
     * </ul>
     * Bundles duplicates are checked by type: only one bundle instance may be registered.
     *
     * @param context bundles context
     */
    public static void initBundles(final ConfigurationContext context) {
        final List<GuiceyBundle> bundles = context.getEnabledBundles();
        final GuiceyBootstrap guiceyBootstrap = new GuiceyBootstrap(context, bundles);
        
        for (GuiceyBundle bundle : new ArrayList<>(bundles)) {
            // iterating bundles as tree in order to detect cycles
            initBundle(Collections.emptyList(), bundle, bundles, context, guiceyBootstrap);
        }

        context.lifecycle().bundlesInitialized(context.getEnabledBundles(), context.getDisabledBundles());
    }

    /**
     * Run all enabled bundles.
     *
     * @param context bundles context
     */
    public static void runBundles(final ConfigurationContext context) {
        final GuiceyEnvironment env = new GuiceyEnvironment(context);
        for (GuiceyBundle bundle : context.getEnabledBundles()) {
            bundle.run(env);
        }
        context.lifecycle().bundlesStarted(context.getEnabledBundles());
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
        final List bundles = Lists.newArrayList(resolveBundles(bootstrap, "configuredBundles"));
        bundles.removeIf(o -> !type.isAssignableFrom(o.getClass()));
        return bundles;
    }

    private static void initBundle(final List<Class<? extends GuiceyBundle>> path,
                                   final GuiceyBundle bundle,
                                   final List<GuiceyBundle> wrk,
                                   final ConfigurationContext context,
                                   final GuiceyBootstrap bootstrap) {
        wrk.clear();
        final Class<? extends GuiceyBundle> bundleType = bundle.getClass();

        if (path.contains(bundleType)) {
            final String name = bundleType.getSimpleName();
            throw new IllegalStateException(String.format("Bundles registration loop detected: %s ) -> %s ...",
                    path.stream().map(Class::getSimpleName).collect(Collectors.joining(" -> "))
                            .replace(name, "( " + name), name));
        }
        LOGGER.debug("Initializing bundle ({} level): {}", path.size() + 1, bundleType.getName());

        // disabled bundles are not processed (so nothing will be registered from it)
        // important to check here because transitive bundles may appear to be disabled
        final ItemId id = ItemId.from(bundle);
        if (context.isBundleEnabled(id)) {
            context.openScope(id);
            bundle.initialize(bootstrap);
            context.closeScope();
        }

        if (!wrk.isEmpty()) {
            final List<Class<? extends GuiceyBundle>> nextPath = new ArrayList<>(path);
            nextPath.add(bundleType);
            for (GuiceyBundle nextBundle : new ArrayList<>(wrk)) {
                initBundle(nextPath, nextBundle, wrk, context, bootstrap);
            }
        }
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
            throw new IllegalStateException("Failed to resolve bootstrap field " + field, e);
        }
    }
}
