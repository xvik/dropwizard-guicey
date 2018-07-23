package ru.vyarus.dropwizard.guice.module.yaml;

import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.BasicBeanDescription;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.primitives.Primitives;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.util.Duration;
import io.dropwizard.util.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vyarus.java.generics.resolver.GenericsResolver;
import ru.vyarus.java.generics.resolver.context.GenericsContext;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Analyzes configuration instance, prepared by dropwizard, in order to be able to use configuration
 * values directly (e.g. by path).
 * <p>
 * Use jackson serialization api for configuration introspection. This way everything that is accessible for
 * jackson serialization will be extracted. Jackson will perform all required reflection work and cache it
 * during configuration mapping, so performance should not be harmed at all.
 * <p>
 * Extra generics information is extracted with {@link GenericsResolver} to use all possibly available types
 * information in bindings.
 * <p>
 * To prevent too deep paths:
 * <ul>
 * <li>Object in java, groovy or guava packages are not introspected deeper</li>
 * <li>Stop on implementations of Iterable, Optional, Duration and Size</li>
 * </ul>
 * <p>
 * Common collection types are projected to base interfaces. E.g. even if property declaration would be
 * {@code ArrayList<String>}, binding declaration would be {@code List<String>} (but value type will remain
 * {@code ArrayList<String>} even if no value available).
 *
 * @author Vyacheslav Rusakov
 * @since 04.05.2018
 */
@SuppressWarnings("PMD.GodClass")
public final class ConfigTreeBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigTreeBuilder.class);

    /**
     * Packages to stop types introspection on (for sure non custom pojo types).
     */
    private static final ImmutableSet<String> INTROSPECTION_STOP_PACKAGES = ImmutableSet.of(
            "java.", "groovy.", "com.google.common.collect"
    );

    /**
     * Classes indicating final values (to stop introspection on).
     */
    private static final ImmutableSet<Class> INTROSPECTION_STOP_TYPES = ImmutableSet.of(
            Iterable.class, Optional.class, Duration.class, Size.class
    );

    /**
     * Lower bounds for value types declarations (to use instead of actual implementation for constant declaration).
     */
    private static final ImmutableSet<Class> COMMON_VALUE_TYPES = ImmutableSet.of(
            List.class, Set.class, Map.class, Multimap.class
    );

    private ConfigTreeBuilder() {
    }

    /**
     * Shortcut for {@link #build(Bootstrap, Configuration, boolean)} with enabled introspection.
     *
     * @param bootstrap     bootstrap instance
     * @param configuration configuration instance
     * @return parsed configuration info
     */
    public static ConfigurationTree build(final Bootstrap bootstrap,
                                          final Configuration configuration) {
        return build(bootstrap, configuration, true);
    }

    /**
     * Analyze configuration object to extract bindable parts.
     *
     * @param bootstrap     bootstrap instance
     * @param configuration configuration instance
     * @param introspect    true to introspect configuration object and extract values by path and unique
     *                      sub configurations
     * @return parsed configuration info
     */
    public static ConfigurationTree build(final Bootstrap bootstrap,
                                          final Configuration configuration,
                                          final boolean introspect) {
        final List<Class> roots = resolveRootTypes(new ArrayList<>(), configuration.getClass());
        if (introspect) {
            final List<ConfigPath> content = resolvePaths(
                    bootstrap.getObjectMapper().getSerializationConfig(),
                    null,
                    new ArrayList<>(),
                    configuration.getClass(),
                    configuration,
                    GenericsResolver.resolve(configuration.getClass()));
            final List<ConfigPath> uniqueContent = resolveUniqueTypePaths(content);
            return new ConfigurationTree(roots, content, uniqueContent);
        } else {
            return new ConfigurationTree(roots);
        }
    }

    /**
     * Analyze configuration class structure to extract all classes in hierarchy with all custom
     * interfaces (ignoring, for example Serializable or something like this).
     *
     * @param roots all collected types so far
     * @param type  type to analyze
     * @return all collected types
     */
    @SuppressWarnings("unchecked")
    private static List<Class> resolveRootTypes(final List<Class> roots, final Class type) {
        roots.add(type);
        if (type == Configuration.class) {
            return roots;
        }
        for (Class iface : type.getInterfaces()) {
            if (isInStopPackage(iface)) {
                continue;
            }
            roots.add(iface);
        }
        return resolveRootTypes(roots, type.getSuperclass());
    }

    /**
     * Use jackson serialization api to extract all configuration values with paths from configuration object.
     * Always analyze types, even if actual branch is not present at all (null value) in order to always bind
     * nulls and avoid "Schrodinger's binding" case. In short, bindings should not depend on configuration values
     * (presence).
     * <p>
     * Still, bindings may vary: for example, bound implementations may differ (best example is dropwizard server type),
     * as a consequences, parsed type may be different and so different properties paths could be recognized.
     *
     * @param config  jackson serialization config
     * @param content currently parsed paths
     * @param type    analyzed part type
     * @param object  analyzed part instance (may be null)
     * @return all configuration paths values
     */
    private static List<ConfigPath> resolvePaths(final SerializationConfig config,
                                                 final ConfigPath root,
                                                 final List<ConfigPath> content,
                                                 final Class type,
                                                 final Object object,
                                                 final GenericsContext genericsContext) {
        final BasicBeanDescription description = config.introspect(
                config.constructType(type)
        );

        for (BeanPropertyDefinition prop : description.findProperties()) {
            // ignore write-only or groovy special property
            if (!prop.couldSerialize() || prop.getName().equals("metaClass")) {
                continue;
            }
            final Object value;
            // if configuration doesn't expect serialization and throws error on access
            // (like netflix dynamic properties) it should not break app startup
            try {
                value = readValue(prop.getAccessor(), object);
            } catch (Exception ex) {
                LOGGER.warn("Can't bind configuration path '{}' due to {}: {}. Enable debug logs to see "
                                + "complete stack trace or use @JsonIgnore on property getter.",
                        fullPath(root, prop), ex.getClass().getSimpleName(), ex.getMessage());
                LOGGER.debug("Complete error: ", ex);
                continue;
            }

            final ConfigPath item = createItem(root, prop, value, genericsContext);
            content.add(item);
            if (root != null) {
                root.getChildren().add(item);
            }

            if (item.isCustomType()) {
                // build generics context for actual value type (if not null)
                final GenericsContext subContext = prop.getGetter() != null
                        ? genericsContext.method(prop.getGetter().getAnnotated()).returnTypeAs(item.getValueType())
                        : genericsContext.fieldTypeAs(prop.getField().getAnnotated(), item.getValueType());

                resolvePaths(config, item, content, item.getValueType(),
                        item.getValue(), subContext);
            }
        }
        if (root != null) {
            // simple properties goes up and composite objects go lower (both groups sorted alphabetically)
            root.getChildren().sort(Comparator.comparing(o -> (o.isCustomType() ? 'b' : 'a') + o.getPath()));
        }
        return content;
    }

    /**
     * Create item for property.
     * <p>
     * Almost always property declaration type is used as binding type (for future binding), but:
     * <ul>
     * <li>If property type is collection implementation, then collection interface used instead</li>
     * <li>If property is Object then type would be taken from value. This means binding type will be Object
     * when value null and actual type when value provided. Assuming this case will not happen (bad config).</li>
     * </ul>
     *
     * @param root            root property (containing), may be null for roots
     * @param prop            jackson property descriptor
     * @param value           property value, may be null
     * @param genericsContext generics context
     * @return path item object
     */
    private static ConfigPath createItem(final ConfigPath root,
                                         final BeanPropertyDefinition prop,
                                         final Object value,
                                         final GenericsContext genericsContext) {
        // need generified type to resolve generics manually because jackson's generics resolution
        // couldn't handle all required cases
        final Type type = prop.getGetter() != null
                ? prop.getGetter().getAnnotated().getGenericReturnType()
                : prop.getField().getAnnotated().getGenericType();
        final Class typeClass = Primitives.wrap(genericsContext.resolveClass(type));

        // upper possible known type (for introspection): ideally type of actually used configuration value
        // note that even when value is null upper type could be different from lower type due to collection projection
        final Class upperType = value == null ? typeClass : value.getClass();
        final boolean customType = isCustomType(upperType);
        // either class declaration or value type (in both cases could be projected to collection interface)
        final boolean objectDeclared = Object.class.equals(typeClass);
        final Class lowerType = correctValueType(objectDeclared ? upperType : typeClass, customType);

        final List<Type> lowerGenerics =
                resolveLowerGenerics(genericsContext, type, typeClass, objectDeclared, lowerType);
        final List<Type> upperGenerics = lowerType.equals(upperType) ? lowerGenerics
                : resolveUpperGenerics(genericsContext, type, objectDeclared, upperType);

        return new ConfigPath(
                root,
                prop.getAccessor().getDeclaringClass(),
                lowerType,
                // as an example, enum constant type could lead to anonymous class
                upperType.isAnonymousClass() ? lowerType : upperType,
                lowerGenerics,
                upperGenerics,
                fullPath(root, prop),
                value,
                customType,
                objectDeclared);
    }

    /**
     * Cases:
     * <ul>
     * <li>Type declared in class and used as is: resolve generics directly from type</li>
     * <li>Type declared, but lower type used (e.g. collection impl declared): track lower type generics</li>
     * <li>Type is Object: resolve generics from declaration (lower type available when value provided)</li>
     * </ul>.
     *
     * @param genericsContext generics context
     * @param type            declared property type
     * @param typeClass       type's class
     * @param objectDeclared  declared type is not Object
     * @param lowerType       selected type from declaration (maybe lower then declared or comletely taken from value)
     * @return lower type generics or empty list
     */
    private static List<Type> resolveLowerGenerics(final GenericsContext genericsContext,
                                                   final Type type,
                                                   final Class typeClass,
                                                   final boolean objectDeclared,
                                                   final Class lowerType) {
        final List<Type> res;
        if (!objectDeclared && !lowerType.equals(typeClass)) {
            // case: collection declared, but not interface.. going to lower type
            res = genericsContext.inlyingType(type).type(lowerType).genericTypes();
        } else {
            // normal case: type analyzed directly
            res = genericsContext.resolveTypeGenerics(objectDeclared ? lowerType : type);
        }
        return res;
    }

    /**
     * Cases:
     * <ul>
     * <li>Object declared in class: nowere to get generics, resolving from type definition</li>
     * <li>Track type generics from declared generics (may not recover all, in this case generics declaration
     * would be used)</li>
     * </ul>.
     * Note that lowerType == upperType case checked outside.
     *
     * @param genericsContext generics context
     * @param type            declared property type
     * @param objectDeclared  declared type is not Object
     * @param upperType       value type (if value available) or declaration type (from class; may be upper then
     *                        lowerType in case of collection)
     * @return upper type generics or empty list
     */
    private static List<Type> resolveUpperGenerics(final GenericsContext genericsContext,
                                                   final Type type,
                                                   final boolean objectDeclared,
                                                   final Class upperType) {
        final List<Type> res;
        if (objectDeclared) {
            // no generics declared in class - use raw generics (from type declaration)
            res = genericsContext.resolveTypeGenerics(upperType);
        } else {
            // computing from actual generic type because it may contain generics, not required for lower type
            // e.g. declaration is ExtraList<String, Integer>  lower type will be List<String>
            // but we need to preserve full generics info for upper type
            res = genericsContext.inlyingTypeAs(type, upperType).genericTypes();
        }
        return res;
    }

    /**
     * Looks for all pojo objects on all configuration paths and select unique pojos. This may be used later
     * to bind configuration part by type (as its uniquely identify location).
     *
     * @param items all parsed configuration paths
     * @return list of unique custom configuration objects
     */
    private static List<ConfigPath> resolveUniqueTypePaths(final List<ConfigPath> items) {
        final Map<Class, ConfigPath> index = new HashMap<>();
        final List<Class> duplicates = new ArrayList<>();
        for (ConfigPath item : items) {
            final Class type = item.getDeclaredType();
            if (!item.isCustomType() || duplicates.contains(type)) {
                continue;
            }
            if (index.containsKey(type)) {
                // type not unique
                index.remove(type);
                duplicates.add(type);
            } else {
                index.put(type, item);
            }
        }
        return index.isEmpty() ? Collections.emptyList() : new ArrayList<>(index.values());
    }

    /**
     * @param type type to check package
     * @return true if type is in introspection stop package, false otherwise
     */
    private static boolean isInStopPackage(final Class type) {
        boolean res = false;
        final String pkg = type.getPackage().getName();
        for (String fnPkg : INTROSPECTION_STOP_PACKAGES) {
            if (pkg.startsWith(fnPkg)) {
                res = true;
                break;
            }
        }
        return res;
    }

    /**
     * @param type type to check
     * @return true if type is data class (could be introspected further), false otherwise
     */
    private static boolean isCustomType(final Class type) {
        boolean res = !type.isPrimitive()
                && !type.isEnum()
                && !type.isArray();

        if (res) {
            // check by package (package could indicate final value to stop introspection)
            res = !isInStopPackage(type);
        }

        if (res) {
            // check if type matches one of indicator types to stop introspection (for sure non custom type)
            res = findMatchingType(type, INTROSPECTION_STOP_TYPES) == null;
        }
        return res;
    }

    @SuppressWarnings("unchecked")
    private static Class correctValueType(final Class type, final boolean custom) {
        Class res = type;
        if (!custom) {
            if (type.isPrimitive()) {
                // unwrap possible primitive to always deal with objects
                res = Primitives.wrap(type);
            } else {
                // use minimal type to simplify binding
                res = MoreObjects.firstNonNull(
                        findMatchingType(type, COMMON_VALUE_TYPES),
                        res);
            }
        }
        return res;
    }

    /**
     * @param type  type to find matching simple type
     * @param types simple types to find compatibility in
     * @return type from provided set if type match found, null if none matches
     */
    @SuppressWarnings("unchecked")
    private static Class findMatchingType(final Class type, final Set<Class> types) {
        Class res = null;
        for (Class val : types) {
            if (val.isAssignableFrom(type)) {
                res = val;
                break;
            }
        }
        return res;
    }

    private static Object readValue(final AnnotatedMember member, final Object object) {
        if (object == null) {
            return null;
        }
        final Object res;
        final AccessibleObject accessor = (AccessibleObject) member.getMember();
        // case: private field
        if (!accessor.isAccessible()) {
            accessor.setAccessible(true);
            try {
                res = member.getValue(object);
            } finally {
                accessor.setAccessible(false);
            }
        } else {
            // public access (most likely getter)
            res = member.getValue(object);
        }
        return res;
    }

    private static String fullPath(final ConfigPath root, final BeanPropertyDefinition prop) {
        return (root == null ? "" : root.getPath() + ".") + prop.getName();
    }
}
