package ru.vyarus.dropwizard.guice.module.context.option.mapper;

import com.google.common.base.Splitter;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.StreamSupport;

/**
 * Simple converter from string source to target type. Supports:
 * <ul>
 * <li>String</li>
 * <li>Boolean</li>
 * <li>Integer</li>
 * <li>Double</li>
 * <li>Short</li>
 * <li>Byte</li>
 * <li>String</li>
 * <li>Enum type value (when target type is exact enum type and string is enum constant)</li>
 * <li>Enum recognition (when target type is generic Enum and string is "fullEnumClass.constantName"</li>
 * </ul>
 * Also, could parse comma-separated lists to arrays of any of the above types (String[], Boolean[] etc).
 * <p>
 * Special support for {@link EnumSet}: it must be a list of fully qualified enum values (enumClass.enumValue).
 *
 * @author Vyacheslav Rusakov
 * @since 27.04.2018
 */
public final class StringConverter {

    private StringConverter() {
    }

    @SuppressWarnings("unchecked")
    public static <V> V convert(final Class<V> target, final String value) {
        final Object res;
        if (target.isArray()) {
            res = handleArray(target.getComponentType(), value);
        } else if (target == EnumSet.class) {
            res = handleEnumSet(value);
        } else {
            res = convertSimple(target, value);
        }
        return (V) res;
    }

    @SuppressWarnings("unchecked")
    private static <V> V[] handleArray(final Class<V> type, final String value) {
        try {
            return StreamSupport.stream(
                    Splitter.on(',').trimResults().omitEmptyStrings().split(value).spliterator(), false)
                    .map(val -> convertSimple(type, val)).toArray(num -> (V[]) Array.newInstance(type, num));
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to parse array " + type.getSimpleName()
                    + "[] from: " + value, ex);
        }
    }

    @SuppressWarnings("unchecked")
    private static EnumSet handleEnumSet(final String value) {
        try {
            return EnumSet.copyOf((List<Enum>) Arrays.asList(handleArray(Enum.class, value)));
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to parse EnumSet from: " + value, ex);
        }
    }

    @SuppressWarnings({"unchecked", "checkstyle:CyclomaticComplexity",
            "PMD.NcssCount", "PMD.CyclomaticComplexity"})
    private static <V> V convertSimple(final Class<V> type, final String value) {
        Object res = null;
        try {
            if (type == String.class) {
                res = value;
            } else if (type == Boolean.class) {
                res = Boolean.valueOf(value);
            } else if (type == Integer.class) {
                res = Integer.valueOf(value);
            } else if (type == Double.class) {
                res = Double.valueOf(value);
            } else if (type == Short.class) {
                res = Short.valueOf(value);
            } else if (type == Byte.class) {
                res = Byte.valueOf(value);
            } else if (type.isEnum()) {
                res = Enum.valueOf((Class) type, value);
            } else if (type == Enum.class) {
                res = parseEnum(value);
            }
        } catch (Exception ex) {
            throw new IllegalStateException(String.format("Failed to convert value '%s' to %s",
                    value, type.getName()), ex);
        }

        if (res == null) {
            throw new IllegalStateException(String.format("Can't convert value '%s': "
                    + "unsupported target type %s", value, type.getName()));
        }
        return (V) res;
    }

    /**
     * Parse enum from full definition: enumClass.enumValue.
     *
     * @param value full enum definition
     * @return parsed enum
     */
    @SuppressWarnings("unchecked")
    private static Enum parseEnum(final String value) {
        final int idx = value.lastIndexOf('.');
        try {
            final Class type = Class.forName(value.substring(0, idx));
            if (!type.isEnum()) {
                throw new IllegalStateException("Type " + type.getName() + " is not enum");
            }
            return Enum.valueOf(type, value.substring(idx + 1));
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to recognize enum value: " + value, ex);
        }
    }
}
