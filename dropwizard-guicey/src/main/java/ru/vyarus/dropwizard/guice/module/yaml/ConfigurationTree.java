package ru.vyarus.dropwizard.guice.module.yaml;

import com.google.common.base.Preconditions;
import io.dropwizard.core.Configuration;
import ru.vyarus.dropwizard.guice.debug.util.RenderUtils;
import ru.vyarus.dropwizard.guice.module.support.ConfigurationTreeAwareModule;

import java.lang.annotation.Annotation;
import java.util.*;
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
 * <p>
 * Object itself could be injected as guice bean {@code @Inject ConfigurationTree config}. Note that it did not contains
 * root configuration instance, only properties tree.
 * <p>
 * Also, object is accessible inside guicey bundles
 * {@link ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyEnvironment#configurationTree()} and guice modules:
 * {@link ConfigurationTreeAwareModule}.
 * <p>
 * Qualified configuration paths also detected: fields or getters annotated with qualifier annotation.
 * Qualifier annotation is an annotation annotated with {@link com.google.inject.BindingAnnotation}
 * (e.g. {@link com.google.inject.name.Named}) or {@link javax.inject.Qualifier}
 * (e.g. {@link javax.inject.Named}) and jakarta annotations.
 *
 * @author Vyacheslav Rusakov
 * @see ru.vyarus.dropwizard.guice.module.yaml.bind.ConfigBindingModule
 * @see ru.vyarus.dropwizard.guice.GuiceyOptions#BindConfigurationByPath
 * @since 04.05.2018
 */
public class ConfigurationTree {

    private static final String DOT = ".";

    // root configuration class, super classes and interfaces
    private final List<Class> rootTypes;
    // all visible internal paths
    private final List<ConfigPath> paths;
    // unique custom types from paths (could be bound by type - no duplicates)
    private final List<ConfigPath> uniqueTypePaths;

    public ConfigurationTree(final List<Class> rootTypes) {
        this(rootTypes, Collections.emptyList(), Collections.emptyList());
    }

    public ConfigurationTree(final List<Class> rootTypes,
                             final List<ConfigPath> paths,
                             final List<ConfigPath> uniqueTypePaths) {
        this.rootTypes = rootTypes;
        this.paths = paths;
        this.uniqueTypePaths = uniqueTypePaths;
        // sort by configuration class and path name for predictable order
        sortContent();
    }

    /**
     * @return configuration hierarchy classes (including {@link io.dropwizard.core.Configuration}) and custom
     * interfaces
     */
    public List<Class> getRootTypes() {
        return new ArrayList<>(rootTypes);
    }

    /**
     * Note that paths are computed using jackson serialization logic. This means that not all deserializable
     * properties could be serialized back, and so not present in the list. For example, if annotated configuration
     * property setter performs immediate value transformation and bean did not provide getter then such value
     * is impossible to read back from configuration.
     * <p>
     * Paths are sorted by configuration class and path name (so more custom properties will be at the beginning and
     * dropwizard properties at the end).
     *
     * @return all configuration value paths (including all steps, e.g. "sub", "sub.smth")
     */
    public List<ConfigPath> getPaths() {
        return new ArrayList<>(paths);
    }

    /**
     * Such unique objects could be used for internal configurations instead of root configuration class
     * (very handy for generic extensions).
     * <p>
     * Note that custom type is unique only if it does not appear anywhere else (on any level).
     * Exact type matching: e.g. if some extending type is declared somewhere it would be considered as different
     * (as class is different, hierarchy not counted).
     *
     * @return list of all configuration value paths, containing unique custom objects
     */
    public List<ConfigPath> getUniqueTypePaths() {
        return new ArrayList<>(uniqueTypePaths);
    }


    // ---------------------------------------------------------- Structure search (tree traverse examples)

    /**
     * Case-insensitive exact match.
     *
     * @param path path to find descriptor for
     * @return path descriptor or null if not found
     */
    public ConfigPath findByPath(final String path) {
        return paths.stream()
                .filter(it -> it.getPath().equalsIgnoreCase(path))
                .findFirst()
                .orElse(null);
    }

    /**
     * Search configuration paths annotated by qualifier annotation. It is not possible to provide the exact annotation
     * instance, but you can create a class implementing annotation and use it for search. For example, guice
     * {@link com.google.inject.name.Named} annotation has {@link com.google.inject.name.Names#named(String)}:
     * it is important that real annotation instance and "pseudo" annotation object would be equal (by equals).
     * <p>
     * For annotations without attributes use annotation type: {@link #findAllByAnnotation(Class)}.
     *
     * @param annotation annotation instance (equal object) to search for annotated config paths
     * @return list of annotated (on field or getter) configuration paths
     */
    public List<ConfigPath> findAllByAnnotation(final Annotation annotation) {
        return paths.stream()
                .filter(path -> path.getQualifier() != null && annotation.equals(path.getQualifier()))
                .collect(Collectors.toList());
    }

    /**
     * Search configuration paths annotated by qualifier annotation (without attributes). For cases when annotation
     * with attributes used - use {@link #findAllByAnnotation(java.lang.annotation.Annotation)} (current method would
     * also work for all annotations because it compares by type only, but it might be not what was searched for).
     *
     * @param qualifierType qualifier annotation type
     * @return list of annotated (on field or getter) configuration paths
     */
    public List<ConfigPath> findAllByAnnotation(final Class<? extends Annotation> qualifierType) {
        return paths.stream()
                .filter(path -> path.getQualifier() != null
                        && qualifierType.equals(path.getQualifier().annotationType()))
                .collect(Collectors.toList());
    }

    /**
     * Search for exactly one configuration path annotated with qualifier annotation. It is not possible to provide
     * the exact annotation instance, but you can create a class implementing annotation and use it for search.
     * For example, guice {@link com.google.inject.name.Named} annotation has
     * {@link com.google.inject.name.Names#named(String)}: it is important that real annotation instance and "pseudo"
     * annotation object would be equal (by equals).
     * <p>
     * For annotations without attributes use annotation type: {@link #findByAnnotation(Class)}.
     *
     * @param annotation annotation instance (equal object) to search for an annotated config path
     * @return found configuration path or null
     * @throws java.lang.IllegalStateException if multiple paths found
     */
    public ConfigPath findByAnnotation(final Annotation annotation) {
        final List<ConfigPath> res = findAllByAnnotation(annotation);
        Preconditions.checkState(res.size() <= 1,
                "Multiple configuration paths qualified with annotation %s:\n%s",
                RenderUtils.renderAnnotation(annotation), res.stream()
                        .map(path -> "\t\t" + path.toString())
                        .collect(Collectors.joining("\n")));
        return res.isEmpty() ? null : res.get(0);
    }

    /**
     * Search for exactly one configuration path annotated with qualified annotation. For cases when annotation with
     * attributes used - use {@link #findByAnnotation(java.lang.annotation.Annotation)} (current method would
     * search only by annotation type, ignoring any (possible) attributes).
     *
     * @param qualifierType qualifier annotation type
     * @return found configuration path or null
     * @throws java.lang.IllegalStateException if multiple paths found
     */
    public ConfigPath findByAnnotation(final Class<? extends Annotation> qualifierType) {
        final List<ConfigPath> res = findAllByAnnotation(qualifierType);
        Preconditions.checkState(res.size() <= 1,
                "Multiple configuration paths qualified with annotation type @%s:\n%s",
                qualifierType.getSimpleName(), res.stream()
                        .map(path -> "\t\t" + path.toString())
                        .collect(Collectors.joining("\n")));
        return res.isEmpty() ? null : res.get(0);
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
     * @return all paths with the same or sub type for specified type or empty list
     */
    public List<ConfigPath> findAllByType(final Class<?> type) {
        return paths.stream()
                // do not allow search for all booleans or integers (completely meaningless)
                .filter(it -> it.isCustomType() && type.isAssignableFrom(it.getDeclaredType()))
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
    public List<ConfigPath> findAllFrom(final Class<? extends Configuration> confType) {
        return paths.stream()
                .filter(it -> it.getRootDeclarationClass() == confType)
                .collect(Collectors.toList());
    }

    /**
     * For example, it would always contain logging, metrics and server paths from dropwizard configuration.
     *
     * @return all root objects and direct root values (only paths 1 level paths)
     * @see #findAllRootPathsFrom(Class)
     */
    public List<ConfigPath> findAllRootPaths() {
        return paths.stream()
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
    public List<ConfigPath> findAllRootPathsFrom(final Class<? extends Configuration> confType) {
        return paths.stream()
                .filter(it -> !it.getPath().contains(DOT) && it.getRootDeclarationClass() == confType)
                .collect(Collectors.toList());
    }


    // ---------------------------------------------------------- Value search


    /**
     * <pre>{@code class Config extends Configuration {
     *          SubConfig sub = { // shown instance contents
     *              String val = "something"
     *          }
     * }}</pre>.
     * {@code valueByPath("sub.val") == "something"}
     * <p>
     * Note: keep in mind that not all values could be accessible (read class javadoc)
     *
     * @param path yaml path (case insensitive)
     * @param <T>  value type
     * @return configuration value on yaml path or null if value is null or path not found
     */
    @SuppressWarnings("unchecked")
    public <T> T valueByPath(final String path) {
        final ConfigPath item = findByPath(path);
        return item != null ? (T) item.getValue() : null;
    }

    /**
     * Useful to resolve sub configuration objects.
     * <pre>{@code class Config extends Configuration {
     *      SubOne sub1 = ...
     *      SubTwo sub2 = ...
     *      SubTwo sub2_1 = ...
     *      SubTwoExt sub2_2 = ... // SubTwoExt extends SubTwo
     * }}</pre>
     * {@code valuesByType(SubOne.class) == [<sub1>]}
     * {@code valuesByType(SubTwo.class) == [<sub2>, <sub2_1>, <sub2_2>]}
     * <p>
     * Note that type matching is not exact: any extending types are also accepted. Type is compared with
     * declaration type (inside configuration class).
     *
     * @param type type of required sub configuration objects
     * @param <T>  value type
     * @return found sub configurations without nulls (properties with null value)
     * @see #valueByUniqueDeclaredType(Class) for uniqe objects access
     */
    @SuppressWarnings("unchecked")
    public <T> List<? extends T> valuesByType(final Class<T> type) {
        return (List<? extends T>) findAllByType(type).stream()
                .map(ConfigPath::getValue)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

    }

    /**
     * Behaviour is the same as {@link #valuesByType(Class)}, but only first element is returned.
     * <p>
     * Note: uniqueness not guaranteed!
     *
     * @param type type of required sub configuration object
     * @param <T>  value type
     * @param <K>  actual expected sub type (may be the same)
     * @return value of first compatible type occurrence (first not null value) or null
     * @see #valueByUniqueDeclaredType(Class) for guaranteed uniquness
     */
    @SuppressWarnings("unchecked")
    public <T, K extends T> K valueByType(final Class<T> type) {
        final List<ConfigPath> items = findAllByType(type)
                .stream().filter(Objects::nonNull).collect(Collectors.toList());
        return items.isEmpty() ? null : (K) items.get(0).getValue();
    }

    /**
     * Search value by unique type declaration.
     * <pre>{@code class Config extends Configuration {
     *      SubOne sub1 = ...
     *      SubTwo sub2 = ...
     *      SubTwoExt sub3 = ...  // SubTwoExt extends SubTwo
     * }}</pre>
     * {@code valueByUniqueDeclaredType(SubOne.class) == <sub1>},
     * {@code valueByUniqueDeclaredType(SubTwo.class) == <sub2>}
     * {@code valueByUniqueDeclaredType(SubTwoExt.class) == <sub3>}
     * <p>
     * Note that direct declaration comparison used! For example, {@code valuesByType(SubTwo) == [<sub2>, <sub3>]}
     * would consider sub2 and sub3 as the same type, but {@code valueByUniqueDeclaredType} will not!
     * <p>
     * Type declaration is not unique if somewhere (maybe in some sub-sub configuration object) declaration with
     * the same type exists. If you need to treat uniqueness only by first path level, then write search
     * function yourself using {@link #findAllRootPaths()} or {@link #findAllRootPathsFrom(Class)}.
     *
     * @param type required target declaration type
     * @param <T>  value type
     * @param <K>  actual expected sub type (may be the same)
     * @return uniquely declared sub configuration object or null if declaration not found or value null
     */
    @SuppressWarnings("unchecked")
    public <T, K extends T> K valueByUniqueDeclaredType(final Class<T> type) {
        return (K) uniqueTypePaths.stream()
                .filter(it -> type.equals(it.getDeclaredType()))
                .findFirst()
                .map(ConfigPath::getValue)
                .orElse(null);
    }

    /**
     * Search configuration values by qualifier annotation. It is not possible to provide the exact annotation instance,
     * but you can create a class implementing annotation and use it for search. For example, guice
     * {@link com.google.inject.name.Named} annotation has {@link com.google.inject.name.Names#named(String)}:
     * it is important that real annotation instance and "pseudo" annotation object would be equal (by equals).
     * <p>
     * For annotations without attributes use annotation type: {@link #annotatedValue(Class)}.
     *
     * @param annotation annotation instance (equal object) to search for annotated config paths
     * @param <T>        target value type
     * @return all non-null annotated configuration values
     * @see #findAllByAnnotation(java.lang.annotation.Annotation)
     */
    @SuppressWarnings("unchecked")
    public <T> Set<T> annotatedValues(final Annotation annotation) {
        return findAllByAnnotation(annotation).stream()
                .map(path -> (T) path.getValue())
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    /**
     * Search configuration values by qualifier annotation (without attributes). For cases when annotation with
     * attributes used - use {@link #annotatedValues(java.lang.annotation.Annotation)} (current method would
     * also work for all annotations because it compares by type only, but it might be not what was searched for).
     *
     * @param qualifierType qualifier annotation type
     * @param <T>           target value type
     * @return all non-null annotated configuration values
     * @see #findAllByAnnotation(Class)
     */
    @SuppressWarnings("unchecked")
    public <T> Set<T> annotatedValues(final Class<? extends Annotation> qualifierType) {
        return findAllByAnnotation(qualifierType).stream()
                .map(path -> (T) path.getValue())
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    /**
     * Search for exactly one qualified configuration value. It is not possible to provide the exact annotation
     * instance, but you can create a class implementing annotation and use it for search. For example, guice
     * {@link com.google.inject.name.Named} annotation has {@link com.google.inject.name.Names#named(String)}:
     * it is important that real annotation instance and "pseudo" annotation object would be equal (by equals).
     * <p>
     * For annotations without attributes use annotation type: {@link #annotatedValue(Class)}.
     *
     * @param annotation annotation instance (equal object) to search for an annotated config path
     * @param <T>        value type
     * @return qualified configuration value or null
     * @throws java.lang.IllegalStateException if multiple values found
     * @see #findByAnnotation(java.lang.annotation.Annotation)
     */
    @SuppressWarnings("unchecked")
    public <T> T annotatedValue(final Annotation annotation) {
        final ConfigPath path = findByAnnotation(annotation);
        return path == null ? null : (T) path.getValue();
    }

    /**
     * Search for exactly one configuration value with qualifier annotation (without attributes). For cases when
     * annotation with attributes used - use {@link #findByAnnotation(java.lang.annotation.Annotation)} (current
     * method would search only by annotation type, ignoring any (possible) attributes).
     *
     * @param qualifierType qualifier annotation type
     * @param <T>           value type
     * @return qualified configuration value or null
     * @throws java.lang.IllegalStateException if multiple values found
     * @see #findByAnnotation(Class)
     */
    @SuppressWarnings("unchecked")
    public <T> T annotatedValue(final Class<? extends Annotation> qualifierType) {
        final ConfigPath path = findByAnnotation(qualifierType);
        return path == null ? null : (T) path.getValue();
    }


    private void sortContent() {
        final Comparator<ConfigPath> comparator = (o1, o2) -> {
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
        };
        paths.sort(comparator);
        uniqueTypePaths.sort(comparator);
    }
}
