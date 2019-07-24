package ru.vyarus.dropwizard.guice.module.context.info;

import ru.vyarus.dropwizard.guice.module.context.info.sign.DisableSupport;

/**
 * Bundle configuration information.
 * For bundles, resolved with bundles lookup mechanism, context would be
 * {@link ru.vyarus.dropwizard.guice.bundle.GuiceyBundleLookup}.
 * <p>
 * Note that the same bundle may be registered by different mechanism simultaneously.
 * For example: by lookup and manually in application class. Bundle will actually be registered either only once
 * (if correct equals method implemented) and it's info will contain 2 context classes
 * ({@link io.dropwizard.Application} and {@link ru.vyarus.dropwizard.guice.bundle.GuiceyBundleLookup})
 * (and {@link #isFromLookup()} will be true) or as separate instances.
 *
 * @author Vyacheslav Rusakov
 * @since 09.07.2016
 */
public interface BundleItemInfo extends InstanceItemInfo, DisableSupport {

    /**
     * @return true if bundle resolved by lookup mechanism, false otherwise
     * @see ru.vyarus.dropwizard.guice.bundle.GuiceyBundleLookup
     */
    boolean isFromLookup();

    /**
     * In case when bundle is registered multiple times, bundle will be transitive if all registrations were transitive.
     *
     * @return true when bundle was registered only by some other bundle (and never directly)
     */
    boolean isTransitive();
}
