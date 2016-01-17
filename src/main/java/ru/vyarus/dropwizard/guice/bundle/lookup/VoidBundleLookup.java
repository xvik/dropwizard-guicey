package ru.vyarus.dropwizard.guice.bundle.lookup;

import ru.vyarus.dropwizard.guice.bundle.GuiceyBundleLookup;
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle;

import java.util.Collections;
import java.util.List;

/**
 * Dummy lookup implementation used to disable bundle lookups.
 *
 * @author Vyacheslav Rusakov
 * @since 16.01.2016
 */
public class VoidBundleLookup implements GuiceyBundleLookup {

    @Override
    public List<GuiceyBundle> lookup() {
        return Collections.emptyList();
    }
}
