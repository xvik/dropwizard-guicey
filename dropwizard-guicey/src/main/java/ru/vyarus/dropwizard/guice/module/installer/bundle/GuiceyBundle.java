package ru.vyarus.dropwizard.guice.module.installer.bundle;

/**
 * Guicey bundle is an enhancement of dropwizard bundles ({@link io.dropwizard.core.ConfiguredBundle}). It allows
 * everything that dropwizard bundles can plus guicey specific features and so assumed to be used instead
 * of dropwizard bundles. But it does not mean that other dropwizard bundles can't be used: both bundle types
 * share the same lifecycle, so you can register dropwizard bundles from within guicey bundle (which is very important
 * for existing bundles re-using).
 * <p>
 * Like dropwizard bundles, guicey bundles contains two lifecycle phases:
 * <ul>
 * <li>initialization - when all bundles (dropwizard and guicey) must be configured</li>
 * <li>run - when dropwizard configuration and environment become available, and some additional guicey
 * configurations may be performed</li>
 * </ul>
 * <p>
 * Extensions and guice modules may be registered (or disabled) in both phases (because guice is not yet started
 * for both), but installers registered only in initialization phase because they are used during classpath
 * scan, performed on dropwizard initialization.
 * <p>
 * Bundles are extremely useful when autoscan is not used to group required extensions installation.
 * <p>
 * Bundle should be registered into {@link ru.vyarus.dropwizard.guice.GuiceBundle} builder.
 * <p>
 * Bundles could be installed automatically with bundle lookups mechanism
 * {@link ru.vyarus.dropwizard.guice.bundle.GuiceyBundleLookup}. For example, it could be service loader based
 * lookup which automatically installs bundle when it appears in classpath.
 * <p>
 * Multiple instances of the same bundle could be registered (like with dropwizard bundles). But guicey deduplicates
 * mechanism will consider equal bundles as duplicate (and register only one). So, to grant bundle uniqueness,
 * properly implement equals method or use
 * {@link ru.vyarus.dropwizard.guice.module.context.unique.item.UniqueGuiceyBundle}. See
 * {@link ru.vyarus.dropwizard.guice.GuiceBundle.Builder#duplicateConfigDetector(
 * ru.vyarus.dropwizard.guice.module.context.unique.DuplicateConfigDetector)} for duplicates detection mechanism info.
 *
 * @author Vyacheslav Rusakov
 * @since 01.08.2015
 */
public interface GuiceyBundle {

    /**
     * Called in initialization phase. {@link GuiceyBootstrap} contains almost the same methods as
     * {@link ru.vyarus.dropwizard.guice.GuiceBundle.Builder}, which allows registering installers, extensions
     * and guice modules. Existing installer could be replaced by disabling old one and registering new.
     * <p>
     * Dropwizard bundles could be also registered with
     * {@link GuiceyBootstrap#dropwizardBundles(io.dropwizard.core.ConfiguredBundle[])} shortcut (or by directly
     * accessing dropwizard bootstrap object: {@link GuiceyBootstrap#bootstrap()}.
     * <p>
     * As bundles could be registered only during initialization phase, it is not possible to
     * avoid bundle registration based on configuration (not a good practice). But, it is possible
     * to use guicey options instead: for example, map option from environment variable and use to decide if some
     * bundles should be activated.
     *
     * @param bootstrap guicey bootstrap object
     * @throws java.lang.Exception if something goes wrong
     */
    default void initialize(final GuiceyBootstrap bootstrap) throws Exception {
        // void
    }

    /**
     * Called on run phase. {@link GuiceyEnvironment} contains almost the same methods as
     * {@link GuiceyBootstrap}, which allows registering extensions and guice modules.
     * <p>
     * Direct jersey specific registrations are possible through shortcuts
     * {@link GuiceyEnvironment#register(Object...)} and {@link GuiceyEnvironment#register(Class[])}.
     * Complete dropwizard environment object is accessible with {@link GuiceyEnvironment#environment()}
     * (assumed that it would not be directly required in most cases).
     * <p>
     * Dropwizard configuration is accessible directly with {@link GuiceyEnvironment#configuration()} and
     * with advanced methods {@link GuiceyEnvironment#configuration(Class)},
     * {@link GuiceyEnvironment#configuration(String)}, {@link GuiceyEnvironment#configurations(Class)} and
     * {@link GuiceyEnvironment#configurationTree()}.
     *
     * @param environment guicey environment object
     * @throws Exception if something goes wrong
     */
    default void run(final GuiceyEnvironment environment) throws Exception {
        // void
    }
}
