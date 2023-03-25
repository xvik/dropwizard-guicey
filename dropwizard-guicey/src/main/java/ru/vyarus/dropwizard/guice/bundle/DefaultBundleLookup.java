package ru.vyarus.dropwizard.guice.bundle;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import ru.vyarus.dropwizard.guice.bundle.lookup.PropertyBundleLookup;
import ru.vyarus.dropwizard.guice.bundle.lookup.ServiceLoaderBundleLookup;
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle;

import java.util.List;

import static ru.vyarus.dropwizard.guice.module.installer.util.Reporter.NEWLINE;
import static ru.vyarus.dropwizard.guice.module.installer.util.Reporter.TAB;

/**
 * Default implementation of bundles lookup mechanism. Did not implement lookup logic but compose more simple
 * lookups. Additionally, logs all resolved bundles.
 * <p>
 * By default includes:
 * <ul>
 * <li>{@link PropertyBundleLookup} to use system property</li>
 * <li>{@link ServiceLoaderBundleLookup} to load bundles using {@link java.util.ServiceLoader} by
 * {@link GuiceyBundle}</li>
 * </ul>
 * Any simple lookup may be registered directly in builder instead of default lookup (the same contract).
 * <p>
 * Additional lookups could be added using {@link #addLookup(GuiceyBundleLookup)} method.
 * Default lookup implementation could be customized by calling constructor with custom loaders list.
 *
 * @author Vyacheslav Rusakov
 * @since 15.01.2016
 */
public class DefaultBundleLookup implements GuiceyBundleLookup {

    private static final Marker MARKER = MarkerFactory.getMarker("bundle reporter");
    private final Logger logger = LoggerFactory.getLogger(DefaultBundleLookup.class);
    private final List<GuiceyBundleLookup> lookups = Lists.newArrayList();

    /**
     * Use predefined lookups.
     */
    public DefaultBundleLookup() {
        this(
                new PropertyBundleLookup(),
                new ServiceLoaderBundleLookup()
        );
    }

    /**
     * @param lookups lookups to use instead of defaults
     */
    @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
    public DefaultBundleLookup(final GuiceyBundleLookup... lookups) {
        for (GuiceyBundleLookup lookup : lookups) {
            addLookup(lookup);
        }
    }

    @Override
    public List<GuiceyBundle> lookup() {
        final List<GuiceyBundle> res = Lists.newArrayList();
        for (GuiceyBundleLookup lookup : lookups) {
            res.addAll(lookup.lookup());
        }
        report(res);
        return res;
    }

    /**
     * Add additional lookup mechanism.
     *
     * @param lookup lookup implementation
     * @return default lookup instance for chained calls
     */
    public DefaultBundleLookup addLookup(final GuiceyBundleLookup lookup) {
        lookups.add(lookup);
        return this;
    }

    private void report(final List<GuiceyBundle> bundles) {
        if (bundles.isEmpty()) {
            return;
        }
        final StringBuilder msg = new StringBuilder("guicey bundles lookup =")
                .append(NEWLINE).append(NEWLINE);
        for (GuiceyBundle bundle : bundles) {
            msg.append(TAB).append(bundle.getClass().getName()).append(NEWLINE);
        }
        logger.info(MARKER, msg.toString());
    }

}
