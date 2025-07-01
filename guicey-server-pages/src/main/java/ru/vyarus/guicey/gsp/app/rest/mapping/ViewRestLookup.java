package ru.vyarus.guicey.gsp.app.rest.mapping;

import com.google.common.base.CharMatcher;
import ru.vyarus.dropwizard.guice.module.installer.util.PathUtils;

import java.util.Map;

/**
 * View rest endpoints are mapped with a prefix: so gsp application call /something could be remapped to
 * [rest]/[prefix]/something. Special prefixes could be mapped to some urls: e.g. /sub/url -&gt; prefix2 and so
 * when /sub/url/something will be called in gsp application it would redirect to [rest]/[prefix2]/something.
 *
 * @author Vyacheslav Rusakov
 * @since 02.12.2019
 */
public class ViewRestLookup {

    private final Map<String, String> prefixes;

    /**
     * Create a view lookup.
     *
     * @param prefixes configured prefixes
     */
    public ViewRestLookup(final Map<String, String> prefixes) {
        // assume immutable map, properly built: keys sorted from longest to smaller (root locations last),
        this.prefixes = prefixes;
    }

    /**
     * @return main mapping prefix
     */
    public String getPrimaryMapping() {
        return prefixes.get("");
    }

    /**
     * @return configured view rest prefixes
     */
    public Map<String, String> getPrefixes() {
        // immutable
        return prefixes;
    }

    /**
     * Lookup target rest context (rest prefix may be registered to sub url). Will select rest prefix context either by
     * sub-url mapping (if url starts with registered sub url) or using root (main) prefix.
     * <p>
     * Knowing sub context is important for templates lookup because in case of detected sub context, rest would be
     * "redirected" under this context, but assets must be resolved with a full path (including this context).
     *
     * @param path gsp application called url (relative to application mapping root)
     * @return target sub context or empty for root context
     */
    public String lookupSubContext(final String path) {
        final String relativePath = CharMatcher.is('/').trimLeadingFrom(path);
        // value will always match to default root mapping if special url mapping not found
        String res = null;
        for (Map.Entry<String, String> entry : prefixes.entrySet()) {
            final String url = entry.getKey();
            if (relativePath.startsWith(url)) {
                // cut off custom app mapping and add correct rest mapping part
                res = url;
                break;
            }
        }
        return res;
    }

    /**
     * @param context context (resolved with {@link #lookupSubContext(String)})
     * @return target rest prefix for provided context
     */
    public String lookupRestPrefix(final String context) {
        return prefixes.get(context);
    }

    /**
     * @param subContext context resolved with {@link #lookupSubContext(String)}
     * @param path       path to resolve
     * @return target rest prefix for provided context
     */
    public String buildRestPath(final String subContext, final String path) {
        final String prefix = prefixes.get(subContext);
        // cut off custom app mapping and add correct rest mapping part
        final String relativeUrl = path.startsWith(subContext) ? path.substring(subContext.length()) : path;
        return PathUtils.path(prefix, relativeUrl);
    }
}
