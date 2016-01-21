package ru.vyarus.dropwizard.guice.bundle.lookup;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import ru.vyarus.dropwizard.guice.bundle.GuiceyBundleLookup;
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle;

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

    public static final String BUNDLES_PROPERTY = "guicey.bundles";

    public List<GuiceyBundle> lookup() {
        final String prop = System.getProperty(BUNDLES_PROPERTY);
        List<GuiceyBundle> res = Collections.emptyList();
        if (prop != null) {
            final Iterable<String> classes = Splitter.on(',').omitEmptyStrings().trimResults().split(prop);
            try {
                res = toInstances(toClasses(classes));
            } catch (Exception e) {
                throw new IllegalStateException(String.format(
                        "Failed to parse bundles system property '%s' value: '%s'",
                        BUNDLES_PROPERTY, prop), e);
            }
        }
        return res;
    }

    /**
     * Sets system property value to provided classes. Shortcut is useful to set property from code, for example,
     * in unit tests.
     *
     * @param bundles bundles to enable
     */
    @SafeVarargs
    public static void enableBundles(final Class<? extends GuiceyBundle>... bundles) {
        final String prop = Joiner.on(',').join(toStrings(Lists.newArrayList(bundles)));
        System.setProperty(BUNDLES_PROPERTY, prop);
    }

    private static List<String> toStrings(final Iterable<Class<? extends GuiceyBundle>> list) {
        final List<String> res = Lists.newArrayList();
        for (Class cls : list) {
            res.add(cls.getName());
        }
        return res;
    }

    @SuppressWarnings("unchecked")
    private List<Class<GuiceyBundle>> toClasses(final Iterable<String> list) throws Exception {
        final List<Class<GuiceyBundle>> res = Lists.newArrayList();
        for (String cls : list) {
            res.add((Class<GuiceyBundle>) Class.forName(cls));
        }
        return res;
    }

    private List<GuiceyBundle> toInstances(final Iterable<Class<GuiceyBundle>> list) throws Exception {
        final List<GuiceyBundle> res = Lists.newArrayList();
        for (Class<GuiceyBundle> cls : list) {
            res.add(cls.newInstance());
        }
        return res;
    }
}
