package ru.vyarus.dropwizard.guice.module.context.info;

import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle;

/**
 * Guicey bundle configuration information.
 * For bundles, resolved with bundles lookup mechanism, context would be
 * {@link ru.vyarus.dropwizard.guice.bundle.GuiceyBundleLookup}.
 * <p>
 * Note that the same bundle may be registered by different mechanism simultaneously.
 * For example: by lookup and manually in application class. Bundle will actually be registered either only once
 * (if correct equals method implemented) and it's info will contain 2 context classes
 * ({@link io.dropwizard.core.Application} and {@link ru.vyarus.dropwizard.guice.bundle.GuiceyBundleLookup})
 * (and {@link #isFromLookup()} will be true) or as separate instances.
 *
 * @author Vyacheslav Rusakov
 * @since 09.07.2016
 */
public interface GuiceyBundleItemInfo extends BundleItemInfo<GuiceyBundle> {

    /**
     * @return true if bundle resolved by lookup mechanism, false otherwise
     * @see ru.vyarus.dropwizard.guice.bundle.GuiceyBundleLookup
     */
    boolean isFromLookup();

    /**
     * Useful for sorting bundles in initialization order (to correctly order transitive bundles).
     *
     * @return initialization order (starting from 1)
     */
    int getInitOrder();
}
