package ru.vyarus.guicey.gsp.app.asset;

import com.google.common.base.CharMatcher;
import com.google.common.collect.Multimap;
import ru.vyarus.dropwizard.guice.module.installer.util.PathUtils;

import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static ru.vyarus.dropwizard.guice.module.installer.util.PathUtils.SLASH;

/**
 * GSP application assets resolution (for exact application with all its extensions).
 * <p>
 * Assets stored in classpath. Multiple classpath locations could be mapped for assets
 * (overriding default location). Some assets could be mapped into exact urls (with ability to override too).
 * <p>
 * Also, assets may be resolved through special class loaders. Possible example is attaching additional
 * folders as resource folder using custom class loader.
 * <p>
 * WARNING: custom class loader will be automatically supported for static resources, but template engine
 * may not be able to resolve template. For example, freemarker use class loader of resource class
 * serving view, so if resource class and view template are in the same class loader then it will work.
 * For sure direct template rendering (without custom resource class) will not work. Freemarker may be configured
 * to support all cases by using custom template loader, activated with global bundle shortcut
 * {@link ru.vyarus.guicey.gsp.ServerPagesBundle.ViewsBuilder#enableFreemarkerCustomClassLoadersSupport()}.
 * Mustache is impossible to configure properly (with current mustache views renderer).
 * The problem with view implementations is that they accept only path to template for rendering and will
 * lookup it by default only in application classloader.
 * <p>
 * Overall, lookup performs two operations: lookup classpath path by url (probably using extra mapping) and
 * resource loading itself. First phase obviously performs lookup too (duplicates second phase), but separate
 * phases are still required to be able to lookup templates directly (using proper class loader).
 *
 * @author Vyacheslav Rusakov
 * @since 26.11.2019
 */
public class AssetLookup implements Serializable {

    /**
     * Primary location is important because assets servlet by default will compute path relative to it.
     */
    private final String primaryLocation;
    /**
     * Mapping of url to classpath package (implicitly sorted by keys).
     */
    private final Multimap<String, String> locations;
    /**
     * Mapping of package to class loader (implicitly sorted by keys).
     */
    private final Multimap<String, ClassLoader> loaders;

    /**
     * Create an assets lookup object.
     *
     * @param primaryLocation primary location
     * @param locations       other locations
     * @param loaders         asset loaders
     */
    public AssetLookup(final String primaryLocation,
                       final Multimap<String, String> locations,
                       final Multimap<String, ClassLoader> loaders) {
        // primary location without leading slash!
        this.primaryLocation = primaryLocation;
        // assume immutable map, properly built: keys sorted from longest to smaller (root locations last),
        // locations for each path are reversed to logically preserve registration order (resource registered later
        // overrides other resources)
        this.locations = locations;
        // assume keys sorted the same, also immutable
        this.loaders = loaders;
    }

    /**
     * @return main application assets classpath path
     */
    public String getPrimaryLocation() {
        return primaryLocation;
    }

    /**
     * @return assets mapping paths
     */
    public Multimap<String, String> getLocations() {
        // immutable
        return locations;
    }

    /**
     * Returned map contains not only custom class loaders but also default class loader (this is important
     * to grant proper resource override sequence). Default class loader is {@link AssetSources#DEFAULT_LOADER}.
     *
     * @return classpath paths mapping to class loaders
     */
    public Multimap<String, ClassLoader> getLoaders() {
        // immutable
        return loaders;
    }

    /**
     * Checks if provided path is an absolute path into primary location and returns relative url instead.
     * It cuts off application url prefix in order to resolve proper path under all registered locations.
     * For example, if application registered for "/app/*" then incoming resource request may be "/app/css/style.css",
     * but actual resource path is "css/style.css" which we will look under classpath (classpath locations
     * also registered by root locations so if app declared its resources under "com.foo.app" package then
     * resource will be searched as "com.foo.app.css/style.css" (the same for all extended locations).
     *
     * @param path possibly absolute path
     * @return relative path (without primary location part if detected)
     */
    public String getRelativePath(final String path) {
        String relativePath = CharMatcher.is('/').trimLeadingFrom(path);
        if (path.startsWith(primaryLocation)) {
            relativePath = path.substring(primaryLocation.length());
        } else if (relativePath.length() == primaryLocation.length() - 1 && primaryLocation.startsWith(relativePath)) {
            // provided path is primary path without trailing slash
            relativePath = "";
        }
        return relativePath;
    }

    /**
     * Lookup asset in classpath by path (relative to application root).
     * Assets, registered to exact path processed in priority. For example, if assets registered for '/foo/bar/'
     * path then path '/foo/bar/sample.css' will be checked first in path-specific assets. Multiple asset packages
     * copuld be configured on each path: assets checked in registration-reverse order to grant regitstration
     * order priority (resources from package, registered later are prioritized).
     * <p>
     * Url may contain application mapping url (so be url relative to server root): {@link #getRelativePath(String)}
     * will be used to cut off mapping prefix.
     *
     * @param url path to find asset for
     * @return matched asset or null if not found
     */
    public URL lookupUrl(final String url) {
        final AssetLocation res = lookup(url);
        return res == null ? null : res.getUrl();
    }

    /**
     * Same as {@link #lookupUrl(String)} but returns resolved absolute location in classpath (may be in custom class
     * loader). This has to be used for templates resolution (views) which does not support direct file specification.
     * It is assumed that later such path will be loaded with {@link #load(String)} which will find proper
     * class loader (again).
     * <p>
     * Url may contain application mapping url (so be url relative to server root): {@link #getRelativePath(String)}
     * will be used to cut off mapping prefix.
     *
     * @param url path to find asset for
     * @return matched absolute classpath path or null if not found
     */
    public String lookupPath(final String url) {
        final AssetLocation res = lookup(url);
        return res == null ? null : res.getPath();
    }

    /**
     * Loads asset from classpath. Will select proper class loader if custom class loaders used.
     * It is assumed to mainly load paths after {@link #lookupPath(String)}, but may be used as general
     * loading mechanism.
     * <p>
     * Will check first if provided path is already an absolute classpath location (assumed to be resolved with
     * {@link #lookupPath(String)}) and if nothing found perform full relative matching with
     * {@link #lookupUrl(String)} (kind of fallback mechanism).
     *
     * @param assetPath exact classpath path
     * @return found resource or null
     */
    public URL load(final String assetPath) {
        URL res;

        // first, assume absolute path search (assumed to be already resolved to correct absolute classpath path)
        // path above may not be prefixed with slash as, even absolute path must be normally searched without it
        // so do more universal (forgiving) logic to reduce errors
        final String path = CharMatcher.is('/').trimLeadingFrom(assetPath);
        AssetLocation location = null;
        // this might not be semantically correct in very complex mapping cases
        for (ClassLoader loader : getMatchingLoaders(path)) {
            location = find(path, loader);
            if (location != null) {
                break;
            }
        }
        // look context class loader just in case
        final ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
        if (location == null && contextLoader != null) {
            // an additional try with context loader
            location = find(path, contextLoader);
        }
        res = location == null ? null : location.getUrl();

        // relative search under all registered locations if absolute search failed
        // only to catch some not quite correct usage scenarios
        if (res == null && !assetPath.startsWith(SLASH)) {
            res = lookupUrl(assetPath);
        }
        return res;
    }

    /**
     * General lookup mechanism (used by {@link #lookupUrl(String)} and {@link #lookupPath(String)} shortcuts.
     * <p>
     * Lookup asset in classpath by path (relative to application root).
     * Assets, registered to exact path processed in priority. For example, if assets registered for '/foo/bar/'
     * path then path '/foo/bar/sample.css' will be checked first in path-specific assets. Multiple asset packages
     * could be configured on each path: assets checked in registration-reverse order to grant regitstration
     * order priority (resources from package, registered later are prioritized).
     *
     * @param path path to find asset for
     * @return matched location or null if not found
     */
    public AssetLocation lookup(final String path) {
        final String relativePath = getRelativePath(path);
        AssetLocation res = null;
        final String assetPath = CharMatcher.is('/').trimLeadingFrom(relativePath);
        for (String subUrl : locations.keySet()) {
            if (assetPath.startsWith(subUrl)) {
                // root locations path will go last and will be ''
                final String targetPath = subUrl.length() > 0 ? assetPath.substring(subUrl.length()) : assetPath;
                for (String pkg : locations.get(subUrl)) {
                    // class loaders registered by root package so we can get all registered by the known root package
                    res = find(pkg + targetPath, loaders.get(pkg));
                    if (res != null) {
                        break;
                    }
                }
                if (res != null) {
                    break;
                }
            }
        }
        return res;
    }

    /**
     * Supposed to be used for error reporting. Return locations in package notion.
     *
     * @param url url relative to application root
     * @return matching classpath locations (where resource may be found)
     */
    public List<String> getMatchingLocations(final String url) {
        final List<String> matches = new ArrayList<>();
        final String relativePath = getRelativePath(url);
        final String assetPath = CharMatcher.is('/').trimLeadingFrom(relativePath);
        for (String subUrl : locations.keySet()) {
            if (assetPath.startsWith(subUrl)) {
                for (String loc : locations.get(subUrl)) {
                    // prefix with folder for better understanding context
                    matches.add(PathUtils.trimSlashes(subUrl + loc).replace("/", "."));
                }
            }
        }
        return matches;
    }

    /**
     * NOTE: method will return not only custom class loaders but also default application loader
     * ({@link AssetSources#DEFAULT_LOADER}) because otherwise it will be impossible to preserve
     * intended loading order.
     * <p>
     * It may return empty set ONLY if  requested absolute path is outside of any registered asset locations
     * (incorrect usage).
     *
     * @param assetPath classpath path (absolute!)
     * @return set (to avoid default classloader duplicates) of custom class loaders
     */
    public Set<ClassLoader> getMatchingLoaders(final String assetPath) {
        // important to cut off leading slash
        final String path = CharMatcher.is('/').trimLeadingFrom(assetPath);
        final Set<ClassLoader> res = new LinkedHashSet<>();
        for (String prefix : loaders.keySet()) {
            if (path.startsWith(prefix)) {
                res.addAll(loaders.get(prefix));
            }
        }
        return res;
    }

    private AssetLocation find(final String path, final Iterable<ClassLoader> loaders) {
        AssetLocation res = null;
        for (ClassLoader loader : loaders) {
            res = find(path, loader);
            if (res != null) {
                break;
            }
        }
        return res;
    }

    private AssetLocation find(final String path, final ClassLoader loader) {
        final URL target = loader.getResource(path);
        return target != null ? new AssetLocation(path, loader, target) : null;
    }

    /**
     * Represent resolved asset location: actual classpath path (absolute) and target class loader (where resource
     * found). Object used to cover various situations: only target classpath path required or file itself.
     */
    public static class AssetLocation {
        private final String path;
        private final ClassLoader loader;
        private final URL url;

        /**
         * Create an asset location.
         *
         * @param path   local path
         * @param loader asset loader
         * @param url    target url
         */
        public AssetLocation(final String path, final ClassLoader loader, final URL url) {
            // important to indicate absolute path
            this.path = PathUtils.leadingSlash(path);
            this.loader = loader;
            this.url = url;
        }

        /**
         * @return absolute asset classpath path
         */
        public String getPath() {
            return path;
        }

        /**
         * @return class loader containing asset
         */
        public ClassLoader getLoader() {
            return loader;
        }

        /**
         * @return url to asset file
         */
        public URL getUrl() {
            return url;
        }
    }
}
