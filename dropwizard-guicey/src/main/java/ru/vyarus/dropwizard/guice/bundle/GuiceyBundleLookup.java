package ru.vyarus.dropwizard.guice.bundle;

import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle;

import java.util.List;

/**
 * Lookups {@link GuiceyBundle} instances (for example from system property).
 * Used to implement plug-and-play behaviour for external modules.
 *
 * @author Vyacheslav Rusakov
 * @since 15.01.2016
 */
@FunctionalInterface
public interface GuiceyBundleLookup {

    /**
     * Called before guice injector creation to lookup additional bundles (in dropwizard run phase).
     *
     * @return list of found bundles or empty list
     */
    List<GuiceyBundle> lookup();
}
