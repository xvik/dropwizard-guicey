package ru.vyarus.guicey.gsp.app.asset;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import ru.vyarus.dropwizard.guice.module.installer.util.PathUtils;

import static ru.vyarus.dropwizard.guice.module.installer.util.PathUtils.SLASH;

/**
 * Application classpath resources configuration object. Assets may be merged from multiple classpath locations.
 * Moreover, classpath locations could be mapped to exact urls.
 * <p>
 * If assets available only through custom class loader then it must be specified, otherwise application
 * class loader would be used (and, most likely, will not find required resources).
 *
 * @author Vyacheslav Rusakov
 * @since 28.11.2019
 */
public class AssetSources {

    /**
     * Default class loader used for assets loading.
     */
    public static final ClassLoader DEFAULT_LOADER = AssetLookup.class.getClassLoader();

    // sub url --> package mapping
    private final Multimap<String, String> locations = LinkedHashMultimap.create();
    // root package --> class loader mapping
    private final Multimap<String, ClassLoader> loaders = LinkedHashMultimap.create();

    /**
     * Register one root asset location.
     *
     * @param location asset classpath location
     */
    public void attach(final String location) {
        attach(SLASH, location);
    }

    /**
     * Register location for exact url path (path-mapped locations override root mappings too).
     * <p>
     * Internally, path used without first slash to simplify matching. Location could be declared as pure package
     * ('dot' separated path).
     *
     * @param url      sub url
     * @param location asset classpath location
     */
    public void attach(final String url, final String location) {
        attach(url, location, null);
    }

    /**
     * Same as {@link #attach(String)} but with custom class loader to use for assets loading.
     * <p>
     * WARNING: only freemarker could see templates from this loader and only if support activated
     * {@link ru.vyarus.guicey.gsp.ServerPagesBundle.ViewsBuilder#enableFreemarkerCustomClassLoadersSupport()}
     *
     * @param location asset classpath location
     * @param loader   class loader to use for assets loading (may be null to use default)
     */
    public void attach(final String location, final ClassLoader loader) {
        attach(SLASH, location, loader);
    }

    /**
     * Same as {@link #attach(String, String)} but with custom class loader to use for assets loading.
     * <p>
     * WARNING: only freemarker could see templates from this loader and only if support activated
     * {@link ru.vyarus.guicey.gsp.ServerPagesBundle.ViewsBuilder#enableFreemarkerCustomClassLoadersSupport()}
     *
     * @param url      sub url
     * @param location assets classpath location
     * @param loader   class loader to use for assets loading (may be null to use default)
     */
    public void attach(final String url, final String location, final ClassLoader loader) {
        final String path = PathUtils.normalizeClasspathPath(location);
        locations.put(PathUtils.normalizeRelativePath(url), path);
        // always use default loader to preserve class loaders order (otherwise overrides would not always work)
        // duplicates will be avoided automatically (multimap does not allow duplicates, but preserve order)
        loaders.put(path, loader == null ? DEFAULT_LOADER : loader);
    }

    /**
     * @return configured assets classpath locations by url
     */
    public Multimap<String, String> getLocations() {
        return locations;
    }

    /**
     * NOTE: default loader ({@link #DEFAULT_LOADER}) will be registered for each package. No duplicates are possible.
     *
     * @return configured class loaders for packages
     */
    public Multimap<String, ClassLoader> getLoaders() {
        return loaders;
    }

    /**
     * Merge assets configurations (in-app config with global extensions).
     *
     * @param assets other assets configuration
     */
    public void merge(final AssetSources assets) {
        this.locations.putAll(assets.locations);
        this.loaders.putAll(assets.loaders);
    }
}
