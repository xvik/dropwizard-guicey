package ru.vyarus.dropwizard.guice.module.installer.util;

import com.google.common.base.MoreObjects;
import com.google.common.base.Stopwatch;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import io.dropwizard.core.setup.Bootstrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vyarus.dropwizard.guice.module.context.ConfigItem;
import ru.vyarus.dropwizard.guice.module.context.ConfigurationContext;
import ru.vyarus.dropwizard.guice.module.context.info.ItemId;
import ru.vyarus.dropwizard.guice.module.context.stat.DetailStat;
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
 * Utility class to work with registered {@link io.dropwizard.core.ConfiguredBundle} objects within dropwizard
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
     * @param context configuration context
     */
    public static void initBundles(final ConfigurationContext context) {
        final List<Class<? extends GuiceyBundle>> path = new ArrayList<>();
        final List<GuiceyBundle> bundles = context.getEnabledBundles();
        final List<GuiceyBundle> initOrder = new ArrayList<>();
        final GuiceyBootstrap guiceyBootstrap = new GuiceyBootstrap(context, path, initOrder);

        initBundles(context, guiceyBootstrap, path, initOrder, bundles);
        context.storeBundlesInitOrder(initOrder);
        context.lifecycle().bundlesInitialized(new ArrayList<>(initOrder), context.getDisabledBundles(),
                context.getIgnoredItems(ConfigItem.Bundle));
    }

    /**
     * Point of root (registered in guice bundle) bundles installation. Also, called in guicey bootstrap
     * for transitive bundles installation (immediate initialization).
     *
     * @param context   configuration context
     * @param bootstrap guicey bootstrap object
     * @param path      transitive bundles installation path
     * @param bundles   bundles to install
     */
    public static void initBundles(final ConfigurationContext context,
                                   final GuiceyBootstrap bootstrap,
                                   final List<Class<? extends GuiceyBundle>> path,
                                   final List<GuiceyBundle> initOrder,
                                   final List<GuiceyBundle> bundles) {
        for (GuiceyBundle bundle : bundles) {
            // iterating bundles as tree in order to detect cycles
            initBundle(path, initOrder, bundle, context, bootstrap);
        }
    }

    /**
     * Run all enabled bundles (and delayed configurations).
     *
     * @param context bundles context
     * @throws Exception if something goes wrong
     */
    public static void runBundles(final ConfigurationContext context) throws Exception {
        final GuiceyEnvironment env = new GuiceyEnvironment(context);
        // process delayed configurations before bundles
        context.processDelayedConfigurations(env);
        // important to process bundles in the same order as they were initialized
        final List<GuiceyBundle> bundlesOrdered = context.getBundlesOrdered();
        for (GuiceyBundle bundle : bundlesOrdered) {
            final Stopwatch timer = context.stat().detailTimer(DetailStat.BundleRun, bundle.getClass());
            bundle.run(env);
            timer.stop();
        }
        context.lifecycle().bundlesStarted(bundlesOrdered);
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

    @SuppressWarnings("PMD.PrematureDeclaration")
    private static void initBundle(final List<Class<? extends GuiceyBundle>> path,
                                   final List<GuiceyBundle> initOrder,
                                   final GuiceyBundle bundle,
                                   final ConfigurationContext context,
                                   final GuiceyBootstrap bootstrap) {
        final Class<? extends GuiceyBundle> bundleType = bundle.getClass();

        if (path.contains(bundleType)) {
            final String name = bundleType.getSimpleName();
            throw new IllegalStateException(String.format("Bundles registration loop detected: %s ) -> %s ...",
                    path.stream().map(Class::getSimpleName).collect(Collectors.joining(" -> "))
                            .replace(name, "( " + name), name));
        }
        // same path instance used for all bundles installation, so it's important to clear its state
        path.add(bundleType);
        LOGGER.debug("Initializing bundle ({} level): {}", path.size(), bundleType.getName());

        // disabled bundles are not processed (so nothing will be registered from it)
        // important to check here because transitive bundles may appear to be disabled
        final ItemId id = ItemId.from(bundle);
        if (context.isBundleEnabled(id)) {
            final ItemId currentScope = context.replaceContextScope(id);
            final Stopwatch timer = context.stat().detailTimer(DetailStat.BundleInit, bundleType);
            try {
                bundle.initialize(bootstrap);
            } catch (Exception ex) {
                Throwables.throwIfUnchecked(ex);
                throw new IllegalStateException("Guicey bundle initialization failed", ex);
            }
            timer.stop();
            // collect bundles in initialization order to run at the same order
            initOrder.add(bundle);
            context.replaceContextScope(currentScope);
        }
        path.remove(bundleType);
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
