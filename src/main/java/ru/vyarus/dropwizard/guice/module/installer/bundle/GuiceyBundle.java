package ru.vyarus.dropwizard.guice.module.installer.bundle;

/**
 * Guicey bundle is very similar to dropwizard {@link io.dropwizard.Bundle}.
 * It may be used for installers or extensions registration (or installers substitution).
 * Bundles may be useful when autoscan is not used to simplify configuration.
 * <p>Bundle should be registered into {@link ru.vyarus.dropwizard.guice.GuiceBundle} builder.</p>
 * <p>Dropwizard bundle may also be guicey bundle (in order to use single extension mechanism).
 * By default, dropwizard bundles lookup is disabled, to enable it use
 * {@link ru.vyarus.dropwizard.guice.GuiceBundle.Builder#configureFromDropwizardBundles(boolean)}.
 * When enabled, all registered bundles are checked if they implement {@link GuiceyBundle}.</p>
 *
 * @author Vyacheslav Rusakov
 * @since 01.08.2015
 */
public interface GuiceyBundle {

    /**
     * Called in run phase. {@link GuiceyBootstrap} contains almost the same methods as
     * {@link ru.vyarus.dropwizard.guice.GuiceBundle.Builder}, which allows to register installers, extensions
     * and guice modules. Existing installer could be replaced by disabling old one and registering new.
     * <p>WARNING: don't assume that this method will be called before or after dropwizard bundle run method
     * (both possible). If configuration or environment objects required, they may be obtained from bootstrap.</p>
     * @param bootstrap guicey bootstrap object
     */
    void initialize(GuiceyBootstrap bootstrap);
}
