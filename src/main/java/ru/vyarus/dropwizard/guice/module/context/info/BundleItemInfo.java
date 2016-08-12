package ru.vyarus.dropwizard.guice.module.context.info;

/**
 * Bundle configuration information.
 * In contrast to other items, may have 2 more registration contexts:
 * {@link io.dropwizard.Bundle} for bundles recognized from dropwizard bundles and
 * {@link ru.vyarus.dropwizard.guice.bundle.GuiceyBundleLookup} from bundles resolved by lookup mechanism.
 * <p>
 * Note that the same bundle may be registered by different mechanism simultaneously.
 * For example: by lookup and manually in application class. Bundle will actually be registered only once, but it's info
 * will contain 2 context classes ({@link io.dropwizard.Application} and
 * {@link ru.vyarus.dropwizard.guice.bundle.GuiceyBundleLookup}) and {@link #isFromLookup()} will be true.
 *
 * @author Vyacheslav Rusakov
 * @since 09.07.2016
 */
public interface BundleItemInfo extends ItemInfo {

    /**
     * @return true if bundle resolved by lookup mechanism, false otherwise
     * @see ru.vyarus.dropwizard.guice.bundle.GuiceyBundleLookup
     */
    boolean isFromLookup();

    /**
     * @return true if bundle recognized from configured dropwizard {@link io.dropwizard.Bundle}, false otherwise
     * @see ru.vyarus.dropwizard.guice.GuiceBundle.Builder#configureFromDropwizardBundles()
     */
    boolean isFromDwBundle();
}
