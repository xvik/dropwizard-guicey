package ru.vyarus.guicey.gsp.app.rest.log;

import ru.vyarus.dropwizard.guice.module.installer.util.PathUtils;

/**
 * Gsp application view rest mapping. Represent mapping of sub url into prefixed rest.
 * <p>
 * Mapping scheme: {@code /[mapping path] ->  [rest prefix]/path}.
 *
 * @author Vyacheslav Rusakov
 * @since 05.12.2019
 */
public class MappedViewPath {
    private final ViewPath path;
    private final String mapping;
    private final String prefix;

    /**
     * @param path    rest path
     * @param mapping sub path
     * @param prefix  rest prefix
     */
    public MappedViewPath(final ViewPath path, final String mapping, final String prefix) {
        this.path = path;
        this.mapping = mapping;
        this.prefix = prefix;
    }

    /**
     * @return hidden rest path
     */
    public ViewPath getPath() {
        return path;
    }

    /**
     * Returned url never starts with slash, but alwasys ends with slash. Empty string used for root path.
     *
     * @return resource prefix mapping url
     */
    public String getMapping() {
        return mapping;
    }

    /**
     * Returned prefix starts and ends with slash.
     *
     * @return rest prefix used for matching.
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * @return server pages application url, leading to this rest method
     */
    public String getMappedUrl() {
        return PathUtils.leadingSlash(PathUtils.path(mapping, path.getUrl().substring(prefix.length())));
    }

    @Override
    public String toString() {
        return path.getMethod().getHttpMethod() + " " + getMappedUrl();
    }
}
