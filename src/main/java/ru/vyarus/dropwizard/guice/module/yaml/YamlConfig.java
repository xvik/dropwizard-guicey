package ru.vyarus.dropwizard.guice.module.yaml;

import io.dropwizard.Configuration;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Detailed yaml configuration. This object provides direct access to sub configuration objects and
 * configuration values by yaml path. This is handy in many cases when only one configuration value is required
 * in class, but entire configuration object have to be bound to access it. Unique sub configuration objects may be
 * used by re-usable parts: you don't need to know actual root configuration class, just be sure that sub configuration
 * class is present somewhere inside only once (e.g. database configuration object).
 * <p>
 * Used for configuration bindings in guice context.
 * <p>
 * Configuration is introspected using jersey serialization api: this means only values visible for jersey
 * serialization will be presented.
 * <p>
 * Each configuration property descriptor provides access to root and child paths, so tree-like traversals are possible
 * (see find* methods below for examples).
 *
 * @author Vyacheslav Rusakov
 * @see ru.vyarus.dropwizard.guice.module.yaml.module.ConfigBindingModule
 * @since 04.05.2018
 */
public class YamlConfig {

    private static final String DOT = ".";

    // root configuration class, super classes and interfaces
    private final List<Class> rootTypes;
    // all visible internal paths
    private final List<YamlConfigItem> contents;
    // unique custom types from paths (could be bound by type - no duplicates)
    private final List<YamlConfigItem> uniqueContentTypes;

    public YamlConfig(final List<Class> rootTypes,
                      final List<YamlConfigItem> contents,
                      final List<YamlConfigItem> uniqueContentTypes) {
        this.rootTypes = rootTypes;
        this.contents = contents;
        this.uniqueContentTypes = uniqueContentTypes;
        // sort by configuration class and path name for predictable order
        sortContent();
    }

    /**
     * @return configuration hierarchy classes (including {@link io.dropwizard.Configuration}) and custom interfaces
     */
    public List<Class> getRootTypes() {
        return rootTypes;
    }

    /**
     * Note that paths are computed using jackson serialization logic. This means that not all deserializable
     * properties could be serialized back, and so not present in the list. For example, if annotated configuration
     * property setter performs immediate value transformation and bean did not provide getter then such value
     * is impossible to read back from configuration.
     * <p>
     * Paths are sorted by configration class and path name (so more custom properties will be at the beginning and
     * dropwizard properties at the end).
     *
     * @return all configuration value paths
     */
    public List<YamlConfigItem> getContents() {
        return contents;
    }

    /**
     * Such unique objects could be used for internal configurations instead of root configuration class
     * (very handy for generic extensions).
     *
     * @return sublist of all configuration value paths, containing unique custom objects
     */
    public List<YamlConfigItem> getUniqueContentTypes() {
        return uniqueContentTypes;
    }

    /**
     * Case insensitive exact match.
     *
     * @param path path to find descriptor for
     * @return path descriptor or null if not found
     */
    public YamlConfigItem findByPath(final String path) {
        return contents.stream()
                .filter(it -> it.getPath().equalsIgnoreCase(path))
                .findFirst()
                .orElse(null);
    }

    /**
     * Useful for searching multiple custom types.
     * <pre>{@code class Config {
     *      SubConf field1;
     *      // just to show that value type taken into account
     *      Object field2 = new SubConfExt();
     * }}</pre>
     * {@code findAllByType(SubConf.class) == [filed1, field2]} because filed1 is declared with required type and
     * field2 value is compatible with requested type.
     *
     * @param type type to search for
     * @return all paths with the same or subtype for provided or empty list
     */
    public List<YamlConfigItem> findAllByType(final Class<?> type) {
        return contents.stream()
                .filter(it -> type.isAssignableFrom(it.getValueType()))
                .collect(Collectors.toList());
    }

    /**
     * Useful for getting all configurations for exact configuration class.
     * <pre>{@code class MyConf extends Configuration {
     *      String val
     *      SubConf sub
     * }}</pre>
     * {@code findAllFrom(MyConf) == [val, sub, sub.val1, sub.val2]} - all property paths, started in
     * this class (all properties from {@code Configuration} are ignored).
     *
     * @param confType configuration type to get all properties from
     * @return all properties declared in (originated in for sub object paths) required configuration class.
     */
    public List<YamlConfigItem> findAllFrom(final Class<? extends Configuration> confType) {
        return contents.stream()
                .filter(it -> it.getRootDeclarationClass() == confType)
                .collect(Collectors.toList());
    }

    /**
     * For example, it would always contain logging, metrics and server paths from dropwizard configuration.
     *
     * @return all root objects and direct root values (only paths 1 level paths)
     * @see #findAllRootPathsFrom(Class)
     */
    public List<YamlConfigItem> findAllRootPaths() {
        return contents.stream()
                .filter(it -> !it.getPath().contains(DOT))
                .collect(Collectors.toList());
    }

    /**
     * The same as {@link #findAllRootPaths()}, but returns only paths started in provided configuration.
     *
     * @param confType configuration type to get all properties from
     * @return all root objects and direct root values (only paths 1 level paths) declared in
     * specified configuration class (directly)
     * @see #findAllRootPaths()
     */
    public List<YamlConfigItem> findAllRootPathsFrom(final Class<? extends Configuration> confType) {
        return contents.stream()
                .filter(it -> !it.getPath().contains(DOT) && it.getRootDeclarationClass() == confType)
                .collect(Collectors.toList());
    }

    private void sortContent() {
        contents.sort((o1, o2) -> {
            final int res;
            final Class rootClass1 = o1.getRootDeclarationClass();
            final Class rootClass2 = o2.getRootDeclarationClass();
            // sort by declaring configuration class to show custom properties first
            if (!rootClass1.equals(rootClass2)) {
                res = Integer.compare(rootTypes.indexOf(rootClass1), rootTypes.indexOf(rootClass2));
            } else {
                // under the same class sort by path
                res = o1.getPath().compareTo(o2.getPath());
            }
            return res;
        });
    }
}
