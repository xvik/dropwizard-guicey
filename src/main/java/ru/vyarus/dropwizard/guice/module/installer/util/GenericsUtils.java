package ru.vyarus.dropwizard.guice.module.installer.util;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.List;

/**
 * Generics utilities.
 *
 * @author Vyacheslav Rusakov
 * @since 22.10.2014
 */
public final class GenericsUtils {

    private GenericsUtils() {
    }

    /**
     * Method returns types instead of classes in order to support complex generics.
     *
     * @param type            inspecting type
     * @param targetInterface target interface to find generics on
     * @return found interface generics or null if not found (e.g. type doesn't implement interface)
     */
    public static Type[] getInterfaceGenerics(final Class<?> type, final Class<?> targetInterface) {
        Type[] args = null;
        Class<?> supertype = type;
        while (args == null && supertype != null && Object.class != supertype) {
            for (Type iface : supertype.getGenericInterfaces()) {
                if (iface instanceof ParameterizedType
                        && targetInterface.equals(((ParameterizedType) iface).getRawType())) {
                    args = ((ParameterizedType) iface).getActualTypeArguments();
                    break;
                }
            }
            supertype = supertype.getSuperclass();
        }
        return args;
    }

    /**
     * @param type            inspecting type
     * @param targetInterface target interface to find generics on
     * @return String representation of interface generics
     */
    public static String[] getInterfaceGenericsAsStrings(final Class<?> type, final Class<?> targetInterface) {
        final Type[] types = getInterfaceGenerics(type, targetInterface);
        final String[] res = new String[types.length];
        for (int i = 0; i < types.length; i++) {
            res[i] = toStringType(types[i]);
        }
        return res;
    }

    @SuppressWarnings("PMD.UseStringBufferForStringAppends")
    private static String toStringType(final Type type) {
        String res;
        if (type instanceof Class) {
            res = ((Class) type).getSimpleName();
        } else if (type instanceof ParameterizedType) {
            final ParameterizedType parametrized = (ParameterizedType) type;
            res = toStringType(parametrized.getRawType());
            final List<String> args = Lists.newArrayList();
            for (Type t : parametrized.getActualTypeArguments()) {
                args.add(toStringType(t));
            }
            if (!args.isEmpty()) {
                res += "<" + Joiner.on(",").join(args) + ">";
            }
        } else if (type instanceof GenericArrayType) {
            res = toStringType(((GenericArrayType) type).getGenericComponentType()) + "[]";
        } else {
            // deep generics nesting case - actual type is top class generic
            // no need to support this case for now
            res = ((TypeVariable) type).getName();
        }
        return res;
    }
}
