package ru.vyarus.dropwizard.guice.module.context.info;

import ru.vyarus.dropwizard.guice.module.context.info.sign.DisableSupport;

/**
 * Dropwizard bundle configuration information.
 * <p>
 * Note that only directly registered dropwizard bundles (through guicey api) are tracked!
 *
 * @author Vyacheslav Rusakov
 * @since 24.07.2019
 */
public interface DropwizardBundleItemInfo extends InstanceItemInfo, DisableSupport {

    /**
     * In case when bundle is registered multiple times, bundle will be transitive if all registrations were transitive.
     *
     * @return true when bundle was registered only by some other bundle (and never directly)
     */
    boolean isTransitive();
}
