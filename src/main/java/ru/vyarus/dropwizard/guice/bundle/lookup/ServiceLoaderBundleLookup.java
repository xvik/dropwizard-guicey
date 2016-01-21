package ru.vyarus.dropwizard.guice.bundle.lookup;

import com.google.common.collect.Lists;
import ru.vyarus.dropwizard.guice.bundle.GuiceyBundleLookup;
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle;

import java.util.List;
import java.util.ServiceLoader;

/**
 * Load bundles using {@link ServiceLoader} by {@link GuiceyBundle}. Intended to be used for automatic
 * installation of third party extensions.
 * <p>
 * Extension jar must contain file:
 * {@code META-INF/services/ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle}.
 * File must contain one or more implementation classes (per line): {@code com.foo.MyGuiceyBundle}.
 * Each bundle must have default no-args constructor.
 *
 * @author Vyacheslav Rusakov
 * @since 18.01.2016
 */
public class ServiceLoaderBundleLookup implements GuiceyBundleLookup {

    @Override
    public List<GuiceyBundle> lookup() {
        return Lists.newArrayList(ServiceLoader.load(GuiceyBundle.class));
    }
}
