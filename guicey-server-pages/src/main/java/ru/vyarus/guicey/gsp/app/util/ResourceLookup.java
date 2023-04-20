package ru.vyarus.guicey.gsp.app.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vyarus.guicey.gsp.app.asset.AssetLookup;
import ru.vyarus.guicey.gsp.views.template.TemplateNotFoundException;

/**
 * Utility used to lookup static resources in multiple locations. This is used for applications extensions
 * mechanism when additional resources could be mapped into application from different classpath location.
 *
 * @author Vyacheslav Rusakov
 * @since 04.12.2018
 */
public final class ResourceLookup {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceLookup.class);

    private ResourceLookup() {
    }

    /**
     * Shortcut for {@link AssetLookup#lookupPath(String)} with fail in case of not found template.
     * Here path may be direct url (which must be properly mapped to classpath) or just relative path to search
     * within of registered locations.
     *
     * @param path   static resource path
     * @param assets assets resolution object
     * @return resource location path (first occurrence)
     * @throws TemplateNotFoundException if template not found
     */
    public static String lookupOrFail(final String path, final AssetLookup assets)
            throws TemplateNotFoundException {
        final String lookup = assets.lookupPath(path);
        if (lookup == null) {
            final String err = String.format(
                    "Template %s not found in locations: %s", path, assets.getMatchingLocations(path));
            // logged here because exception most likely will be handled as 404 response
            LOGGER.info(err);
            throw new TemplateNotFoundException(err);
        }
        return lookup;
    }

    /**
     * Shortcut for {@link AssetLookup#load(String)} with fail in case of not found template.
     *
     * @param path   path to check (absolute (/) or relative)
     * @param assets assets resolution object
     * @throws TemplateNotFoundException if template not found
     */
    public static void existsOrFail(final String path, final AssetLookup assets) throws TemplateNotFoundException {
        if (assets.load(path) == null) {
            final String err = String.format("Template not found on path %s", path);
            // logged here because exception most likely will be handled as 404 response
            LOGGER.info(err);
            throw new TemplateNotFoundException(err);
        }
    }
}
