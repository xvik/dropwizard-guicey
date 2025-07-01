package ru.vyarus.guicey.gsp.app.rest.log;

import ru.vyarus.dropwizard.guice.module.installer.util.PathUtils;

/**
 * Gsp application hidden mapping. Appears when different rest prefix mapped to sub url (so root prefix resources
 * under the same sub url become hidden).
 *
 * @author Vyacheslav Rusakov
 * @since 05.12.2019
 */
public class HiddenViewPath extends MappedViewPath {
    private final String overridingMapping;

    /**
     * Create a hidden view path.
     *
     * @param path              rest path
     * @param mapping           sub path
     * @param prefix            rest prefix
     * @param overridingMapping mapping, overriding this path
     */
    public HiddenViewPath(final ViewPath path,
                          final String mapping,
                          final String prefix,
                          final String overridingMapping) {
        super(path, mapping, prefix);
        this.overridingMapping = overridingMapping;
    }

    /**
     * Returned url never starts with slash, but always ends with slash.
     *
     * @return hiding mapping (sub url)
     */
    public String getOverridingMapping() {
        return overridingMapping;
    }

    @Override
    public String toString() {
        return super.toString() + " hidden by " + PathUtils.leadingSlash(overridingMapping);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MappedViewPath)) {
            return false;
        }

        // same REWRITTEN(!) urls with the same target endpoint are THE SAME!
        final MappedViewPath that = (MappedViewPath) o;

        return getMappedUrl().equals(that.getMappedUrl()) && getPath().getMethod().equals(that.getPath().getMethod());
    }

    @Override
    public int hashCode() {
        return getMappedUrl().hashCode() + 31 * getPath().hashCode();
    }
}
