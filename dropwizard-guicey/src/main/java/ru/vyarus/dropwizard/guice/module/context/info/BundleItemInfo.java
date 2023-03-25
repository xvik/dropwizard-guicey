package ru.vyarus.dropwizard.guice.module.context.info;

import ru.vyarus.dropwizard.guice.module.context.info.sign.DisableSupport;

/**
 * Base interface for bundle items: guicey and dropwizard bundles.
 *
 * @param <T> instance type
 * @author Vyacheslav Rusakov
 * @since 28.07.2019
 */
public interface BundleItemInfo<T> extends InstanceItemInfo<T>, DisableSupport {

    /**
     * In case when bundle is registered multiple times, bundle will be transitive if all registrations were transitive.
     * Reminder: bundle is registered by instance, but same instance may be registered multiple times. Also,
     * deduplication mechanism could consider different instances as the same bundle (equal objects, by default).
     *
     * @return true when bundle was registered only by some other bundle (and never directly)
     */
    boolean isTransitive();

    /**
     * @return true for dropwizard bundle, false for guicey bundle
     */
    boolean isDropwizard();
}
