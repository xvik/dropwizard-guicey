package ru.vyarus.guicey.gsp.app.ext;

import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyEnvironment;
import ru.vyarus.guicey.gsp.app.asset.AssetSources;
import ru.vyarus.guicey.gsp.app.rest.mapping.ViewRestSources;

/**
 * Assets and views delayed configuration callback for server pages application extensions. Used to perform
 * configurations under run phase.
 *
 * @author Vyacheslav Rusakov
 * @since 29.11.2019
 */
@FunctionalInterface
public interface DelayedConfigurationCallback {

    /**
     * Called under run phase to perform delayed extensions configuration.
     * <p>
     * For assets multiple package registrations per url is allowed, but for views only one prefix could be
     * mapped to url. Most likely, root views mapping will be already configured by
     * application itself (but if not then root mapping may be applied).
     *
     * @param environment guicey environment object
     * @param assets      object for registration of extended locations
     * @param views       object for registration of additional view mappings
     */
    void configure(GuiceyEnvironment environment, AssetSources assets, ViewRestSources views);
}
