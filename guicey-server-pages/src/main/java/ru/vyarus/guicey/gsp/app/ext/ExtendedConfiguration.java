package ru.vyarus.guicey.gsp.app.ext;

import com.google.common.base.Preconditions;
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyEnvironment;
import ru.vyarus.guicey.gsp.app.asset.AssetSources;
import ru.vyarus.guicey.gsp.app.rest.mapping.ViewRestSources;

/**
 * Gsp application extension object. Contains both direct and delayed configurations. Delayed configuration
 * will be applied just before gsp application initialization (after guice context startup).
 *
 * @author Vyacheslav Rusakov
 * @since 26.12.2019
 */
public class ExtendedConfiguration {
    private final String name;
    private final AssetSources assets = new AssetSources();
    private final ViewRestSources views = new ViewRestSources();
    private DelayedConfigurationCallback delayedConfigCallback;

    /**
     * Create configuration.
     *
     * @param name application name
     */
    public ExtendedConfiguration(final String name) {
        this.name = name;
    }

    /**
     * @param callback delayed configuration callback
     */
    public void setDelayedCallback(final DelayedConfigurationCallback callback) {
        Preconditions.checkArgument(this.delayedConfigCallback == null,
                "Only one delayed configuration could be registered");
        this.delayedConfigCallback = callback;
    }

    /**
     * Apply delayed configuration, if registered.
     *
     * @param environment environment
     */
    public void configure(final GuiceyEnvironment environment) {
        if (delayedConfigCallback != null) {
            delayedConfigCallback.configure(environment, assets, views);
        }
    }

    /**
     * @return target application name
     */
    public String getName() {
        return name;
    }

    /**
     * @return assets extensions
     */
    public AssetSources getAssets() {
        return assets;
    }

    /**
     * @return views extensions
     */
    public ViewRestSources getViews() {
        return views;
    }
}
