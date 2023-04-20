package ru.vyarus.dropwizard.guice.module.yaml;

import ru.vyarus.java.generics.resolver.context.container.ParameterizedTypeImpl;
import ru.vyarus.java.generics.resolver.util.GenericsUtils;
import ru.vyarus.java.generics.resolver.util.TypeToStringUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Сonfiguration path's value.  Value is resolved using type information from configuration class
 * and actual instance analysis. This means that item declaration could change if configuration changes.
 * The most obvious example is property-dependent mapping, like server object (when one property could completely
 * change available configuration paths (simple and usual servers)).
 * <p>
 * One edge case is possible (but assumed to never happen): when property declared as Object in class, actual type
 * could be resolved from value. But that means that if value become null, property binding type would change to
 * Object. Assume no one would ever use Object for configuration property (or, if so, value will never be null
 * because otherwise there will be no binding available in both cases).
 * <p>
 * Item contains root and child references and might be traversed like a tree.
 *
 * @author Vyacheslav Rusakov
 * @since 04.05.2018
 */
public class ConfigPath {

    private static final String QUOTE = "\"";

    private final ConfigPath root;
    private final List<ConfigPath> children = new ArrayList<>();
    // configuration class where it was declared (may be sub object class)
    private final Class declarationClass;
    // class declaration (except Object case, when type could be resolved from instance)
    private final Class declaredType;
    // actual value type
    private final Class valueType;
    private final List<Type> declaredTypeGenerics;
    private final List<Type> valueTypeGenerics;
    private final String path;
    private final Object value;
    private final boolean customType;
    private final boolean objectDeclaration;

    @SuppressWarnings({"checkstyle:ParameterNumber", "PMD.ExcessiveParameterList"})
    public ConfigPath(
            final ConfigPath root,
            final Class declarationClass,
            final Class declaredType,
            final Class valueType,
            final List<Type> declaredTypeGenerics,
            final List<Type> valueTypeGenerics,
            final String path,
            final Object value,
            final boolean customType,
            final boolean objectDeclaration) {
        this.declarationClass = declarationClass;
        this.declaredType = declaredType;
        this.valueType = valueType;
        this.declaredTypeGenerics = declaredTypeGenerics;
        this.valueTypeGenerics = valueTypeGenerics;
        this.path = path;
        this.value = value;
        this.customType = customType;
        this.objectDeclaration = objectDeclaration;
        this.root = root;
    }

    /**
     * For example, if current path is "some.long.path" then method returns "some.long" path item.
     *
     * @return parent config level or null for root level
     */
    public ConfigPath getRoot() {
        return root;
    }

    /**
     * For example, if current path is "sub" then returned items would be next level paths: "sub.val1", "sub.val2" etc.
     * Useful for manual config analysis.
     *
     * @return list of child properties
     */
    public List<ConfigPath> getChildren() {
        return children;
    }

    /**
     * Note that this would be last path's part (property) declaration class. For example, for path "some.path"
     * it would be class of "some" (sub object of configuration).
     *
     * @return class where property was declared
     */
    public Class getDeclarationClass() {
        return declarationClass;
    }

    /**
     * If property is declared as collection implementation then collection interface will be used instead.
     * For example, {@code ArrayList<String>} then declaration type would be {@code List<String>}.
     * <p>
     * Edge case possible: if class declaration is Object and value is not null then actual value
     * would be taken from value. This case should never appear, because it's insane to use Object to declare
     * property type.
     *
     * @return type declaration from class
     */
    public Class getDeclaredType() {
        return declaredType;
    }


    /**
     * When value is null, type may be still different from {@link #getDeclaredType()} if declared
     * type was actually some collection implementation.
     *
     * @return actual path value type (from actual instance or declared type, when instance is null)
     */
    public Class getValueType() {
        return valueType;
    }

    /**
     * For example, if value is List then one parameter will be available with list generic type.
     * <p>
     * When actual generics are not set then upper bounds of declared generics will be used. For example,
     * for {@code List} it will be {@code [Object]} and for class {@code Some<T extends Comparable>} -
     * {@code [Comparable]}.
     *
     * @return binding type generics or empty list if not generified type
     * @see #getDeclaredTypeGenericClasses()
     */
    public List<Type> getDeclaredTypeGenerics() {
        return declaredTypeGenerics;
    }

    /**
     * Even when value is null, generics could differ from declared type generics. For example, declaration is
     * {@code ExtraList<String, Integer>}, declaration type {@code List<String>} (so it's generics just [List]).
     * And type generics would be [String, Integer].
     *
     * @return generics of value type or empty list if not generified type
     */
    public List<Type> getValueTypeGenerics() {
        return valueTypeGenerics;
    }

    /**
     * E.g. "some.property.path".
     *
     * @return full yaml path of property
     */
    public String getPath() {
        return path;
    }

    /**
     * @return property value (may be null)
     */
    public Object getValue() {
        return value;
    }

    /**
     * Type considered custom when it's not primitive, collection and few other simple types. In essence, this is
     * type which contains (or might contain) sub paths.
     *
     * @return true when value is sub-configuration object, false when value is just a value
     */
    public boolean isCustomType() {
        return customType;
    }

    /**
     * True means that property declared as Object. In this case type could be resolved from actual value
     * (if it's not null). This should be next to impossible case for sane configurations
     * (who would use Object for configuration property?).
     * <p>
     * Indicator might be important because in this case binding would be of Object type when value is null
     * and with value type for not null value.
     *
     * @return true when property is declared as Object in class, false otherwise
     */
    public boolean isObjectDeclaration() {
        return objectDeclaration;
    }

    /**
     * Useful for quick analysis when deeper generic type knowledge is not important (e.g. to know type in
     * list, set or map).
     *
     * @return declared type generics as class or empty map if not generics
     * @see #getDeclaredTypeGenerics()
     */
    public List<Class> getDeclaredTypeGenericClasses() {
        return getClasses(declaredTypeGenerics);
    }

    /**
     * Useful for quick analysis when deeper generic type knowledge is not important..
     *
     * @return value type generics as class or empty map if not generics
     */
    public List<Class> getValueTypeGenericClasses() {
        return getClasses(valueTypeGenerics);
    }

    /**
     * Useful to filter out core dropwizard properties (where root type would be {@link io.dropwizard.Configuration}.
     *
     * @return root configuration class where entire path started
     */
    public Class getRootDeclarationClass() {
        ConfigPath res = this;
        while (res.getRoot() != null) {
            res = res.getRoot();
        }
        return res.getDeclarationClass();
    }

    /**
     * @return declared type with known generics (parameterized type) or simple class if no generics provided
     */
    public Type getDeclaredTypeWithGenerics() {
        return getType(declaredType, declaredTypeGenerics);
    }

    /**
     * When value null will be the same as {@link #getDeclaredTypeWithGenerics()}. If types are different
     * (value not null), then actual type generics will be tracked from known definition generics.
     *
     * @return value type with generics (parameterized type) or simple clas if not generics provided
     */
    public Type getTypeWithGenerics() {
        return getType(valueType, valueTypeGenerics);
    }

    /**
     * @return last path element (e.g. for "some.long.path" return "path")
     */
    public String getLastPathLevel() {
        final int idx = path.lastIndexOf('.');
        return idx < 0 ? path : path.substring(idx + 1);
    }

    /**
     * @return declared type string including generics
     */
    public String toStringDeclaredType() {
        return TypeToStringUtils.toStringType(getDeclaredTypeWithGenerics(), Collections.emptyMap());
    }

    /**
     * @return value type string including generics
     */
    public String toStringType() {
        return TypeToStringUtils.toStringType(getTypeWithGenerics(), Collections.emptyMap());
    }

    /**
     * Only put string values other than null into quotes (to be more obvious).
     *
     * @return value as string
     */
    public String toStringValue() {
        return value instanceof String ? (QUOTE + value + QUOTE) : String.valueOf(value);
    }

    @Override
    public String toString() {
        final StringBuilder res = new StringBuilder(100)
                .append('[').append(getRootDeclarationClass().getSimpleName()).append("] ")
                .append(path).append(" (").append(toStringDeclaredType());
        // indicate Object declaration in class (should never occur in sane configurations)
        if (isObjectDeclaration() && declaredType != Object.class) {
            res.append('*');
        }
        // if value is null, type will be the same as declared
        if (!declaredType.equals(valueType)) {
            res.append(" as ").append(toStringType());
        }
        res.append(") = ").append(toStringValue());
        return res.toString();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ConfigPath)) {
            return false;
        }

        final ConfigPath that = (ConfigPath) o;
        // dropwizard application always use only one configuration object so only path is meaningful
        return path.equals(that.path);
    }

    @Override
    public int hashCode() {
        // dropwizard application always use only one configuration object so only path is meaningful
        return path.hashCode();
    }

    private List<Class> getClasses(final List<Type> generics) {
        return generics.isEmpty() ? Collections.emptyList()
                // no type variables could appear inside
                : generics.stream()
                .map(type -> GenericsUtils.resolveClass(type, Collections.emptyMap()))
                .collect(Collectors.toList());
    }

    private Type getType(final Class<?> type, final List<Type> generics) {
        return generics.isEmpty()
                ? type
                // outer class required for proper guice binding (if current is inner)
                : new ParameterizedTypeImpl(type, generics.toArray(new Type[0]), type.getEnclosingClass());
    }
}
