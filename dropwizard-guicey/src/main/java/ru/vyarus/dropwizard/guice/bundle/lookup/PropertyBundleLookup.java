package ru.vyarus.dropwizard.guice.bundle.lookup;

import ru.vyarus.dropwizard.guice.bundle.GuiceyBundleLookup;
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle;
import ru.vyarus.dropwizard.guice.module.installer.util.PropertyUtils;

import java.util.Collections;
import java.util.List;

/**
 * Use 'guicey.bundles' system property to lookup bundles. Property value must contain comma-separated list of
 * complete bundle class names. Each bundle in list must have default no-args constructor.
 * <p>
 * Example: {@code -Dguicey.bundles=com.foo.MyBundle1,com.foo.MyBundle2}.
 * <p>
 * Static shortcut method may be used to set property value from code (for example, in unit tests to enable
 * specific debug bundle): {@code PropertyBundleLookup.enableBundles(MyBundle1.class, MyBundle2.class)}.
 * <p>
 * For example, last approach is used in guicey tests to enable
 * {@link ru.vyarus.dropwizard.guice.module.jersey.debug.HK2DebugBundle} to check beans boundaries during tests.
 *
 * @author Vyacheslav Rusakov
 * @since 16.01.2016
 */
public class PropertyBundleLookup implements GuiceyBundleLookup {

    /**
     * Bundles system property.
     */
    public static final String BUNDLES_PROPERTY = "guicey.bundles";

    @Override
    public List<GuiceyBundle> lookup() {
        return PropertyUtils.getProperty(BUNDLES_PROPERTY, Collections.emptyMap());
    }

    /**
     * Sets system property value to provided classes. Shortcut is useful to set property from code, for example,
     * in unit tests.
     *
     * @param bundles bundles to enable
     */
    @SafeVarargs
    public static void enableBundles(final Class<? extends GuiceyBundle>... bundles) {
        PropertyUtils.setProperty(BUNDLES_PROPERTY, bundles);
    }
}
