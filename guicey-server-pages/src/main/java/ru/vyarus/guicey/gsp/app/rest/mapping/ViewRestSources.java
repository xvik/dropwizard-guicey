package ru.vyarus.guicey.gsp.app.rest.mapping;

import com.google.common.base.Preconditions;
import ru.vyarus.dropwizard.guice.module.installer.util.PathUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Application views rest mappings configuration object.
 *
 * @author Vyacheslav Rusakov
 * @since 02.12.2019
 */
public class ViewRestSources {

    private final Map<String, String> prefixes = new HashMap<>();

    /**
     * Register root rest mapping prefix.
     * <p>
     * Registration may be performed only once: overrides are not allowed. Most likely, gsp application will have
     * configured root mapping, so use carefully in extensions.
     *
     * @param prefix view rest mapping prefix
     */
    public void map(final String prefix) {
        map("/", prefix);
    }

    /**
     * Register prefix for exact url path (path-mapped prefixes override root mapping).
     * <p>
     * Internally, path used without first slash to simplify matching.
     * <p>
     * Only one prefix may be registered per url. In case of overriding registration error will be thrown.
     * <p>
     * Pay attention that additional asset locations registration may be required,
     * because only templates relative to view class will be correctly resolved, but direct templates may fail
     * to resolve.
     *
     * @param url    sub url
     * @param prefix asset classpath location
     */
    public void map(final String url, final String prefix) {
        final String path = PathUtils.normalizeRelativePath(url);
        Preconditions.checkArgument(!prefixes.containsKey(path),
                "Can't register views prefix %s mapping for path %s because it's already mapped to %s",
                prefix, url, prefixes.get(path));
        prefixes.put(path, PathUtils.normalizeRelativePath(prefix));
    }

    /**
     * @return primary view rest mapping prefix or null if not configured
     */
    public String getPrimaryPrefix() {
        return prefixes.get("");
    }

    /**
     * @return configured prefixes mappings by url
     */
    public Map<String, String> getPrefixes() {
        return prefixes;
    }

    /**
     * Merge configurations (in-app config with global extensions).
     * <p>
     * Will throw error on configuration override attempt (configuration clash).
     *
     * @param prefixes other prefixes configuration
     */
    public void merge(final ViewRestSources prefixes) {
        this.prefixes.putAll(prefixes.prefixes);
    }
}
