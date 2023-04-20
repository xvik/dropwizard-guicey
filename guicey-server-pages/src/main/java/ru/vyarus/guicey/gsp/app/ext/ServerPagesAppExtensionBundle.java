package ru.vyarus.guicey.gsp.app.ext;

import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle;
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyEnvironment;
import ru.vyarus.guicey.gsp.ServerPagesBundle;
import ru.vyarus.guicey.gsp.app.GlobalConfig;

/**
 * Bundle for extending (or overriding) registered server pages app resources (through
 * {@link ServerPagesBundle#extendApp(String)}).
 * <p>
 * Bundle is registered in initialization phase, but with delayed configuration callback, actual configuration
 * could be delayed to run phase.
 *
 * @author Vyacheslav Rusakov
 * @since 27.09.2019
 */
public class ServerPagesAppExtensionBundle implements GuiceyBundle {

    private final ExtendedConfiguration ext;

    protected ServerPagesAppExtensionBundle(final String name) {
        this.ext = new ExtendedConfiguration(name);
    }

    @Override
    public void run(final GuiceyEnvironment environment) throws Exception {
        environment.<GlobalConfig>sharedStateOrFail(ServerPagesBundle.class,
                "Either server pages support bundle was not installed (use %s.builder() to create bundle) "
                        + " or it was installed after '%s' application extension bundle",
                ServerPagesBundle.class.getSimpleName(), this.ext.getName())
                // delayed callback will be called just before gsp application initialization
                // (after guicey initialization complete, but before jersey init)
                .extendApp(ext);
    }

    /**
     * Extensions bundle builder.
     */
    public static class AppExtensionBuilder {

        private final ServerPagesAppExtensionBundle bundle;
        private final ClassLoader loader;

        public AppExtensionBuilder(final String name, final ClassLoader loader) {
            this.bundle = new ServerPagesAppExtensionBundle(name);
            // if loader is null, default loader would be set automatically by AssetSources
            this.loader = loader;
        }

        /**
         * Map view rest to sub url. May be used to map additional rest endpoints with different prefix.
         * <p>
         * Only one mapping is allowed per url (otherwise error will be thrown)! But mappings for larger sub urls
         * are always allowed (partial override).
         * <p>
         * Normally, application configures root views mapping, but if not, then extension could register root
         * mapping using "/" as url. Direct shortcut not provided because such usage case considered as very rare,
         * <p>
         * Use delayed configuration if dropwizard configuration object is required
         * {@link #delayedConfiguration(DelayedConfigurationCallback)}.
         * <p>
         * Pay attention that additional asset locations may be required ({@link #attachAssets(String, String)},
         * because only templates relative to view class will be correctly resolved, but direct templates may fail
         * to resolve.
         *
         * @param subUrl sub url to map views to
         * @param prefix rest prefix to map as root views
         * @return builder instance for chained calls
         * @see ru.vyarus.guicey.gsp.app.ServerPagesAppBundle.AppBuilder#mapViews(String, String)
         */
        public AppExtensionBuilder mapViews(final String subUrl, final String prefix) {
            bundle.ext.getViews().map(subUrl, prefix);
            return this;
        }

        /**
         * Add additional assets location. Useful for adding new resources or overriding application assets.
         * <p>
         * Use delayed configuration if dropwizard configuration object is required
         * {@link #delayedConfiguration(DelayedConfigurationCallback)}.
         *
         * @param path assets classpath path
         * @return builder instance for chained calls
         * @see ru.vyarus.guicey.gsp.app.ServerPagesAppBundle.AppBuilder#attachAssets(String)
         */
        public AppExtensionBuilder attachAssets(final String path) {
            bundle.ext.getAssets().attach(path, loader);
            return this;
        }

        /**
         * Essentially the same as {@link #attachAssets(String)}, but attach classpath assets to application
         * sub url. As with root assets, multiple packages could be attached to url. Registration order is important:
         * in case if multiple packages contains the same file, file from the latest registered package will be used.
         * <p>
         * Use delayed configuration if dropwizard configuration object is required
         * {@link #delayedConfiguration(DelayedConfigurationCallback)}.
         *
         * @param subUrl sub url to serve assets from
         * @param path   assets classpath paths
         * @return builder instance for chained calls
         * @see ru.vyarus.guicey.gsp.app.ServerPagesAppBundle.AppBuilder#attachAssets(String, String)
         */
        public AppExtensionBuilder attachAssets(final String subUrl, final String path) {
            bundle.ext.getAssets().attach(subUrl, path, loader);
            return this;
        }

        /**
         * Used to delay actual configuration till runtime phase, when dropwizard configuration will be available
         * (or, in case of complex setup, other bundles will perform all required initializations).
         * Called after guicey initialization (when guice injector created and extensions installed).
         * <p>
         * Only one callback may be registered.
         * <p>
         * WARNING: if extension bundle created with custom class loader, it will not be applied to callback
         * configuration because callback is a low level configuration, and it supports custom class loader with an
         * additional parameter.
         *
         * @param callback callback for extensions configuration under run phase
         * @return builder instance for chained calls
         */
        public AppExtensionBuilder delayedConfiguration(final DelayedConfigurationCallback callback) {
            bundle.ext.setDelayedCallback(callback);
            return this;
        }

        /**
         * @return bundle instance
         */
        public ServerPagesAppExtensionBundle build() {
            return bundle;
        }
    }
}
