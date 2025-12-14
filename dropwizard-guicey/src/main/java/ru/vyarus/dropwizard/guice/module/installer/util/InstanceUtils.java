package ru.vyarus.dropwizard.guice.module.installer.util;

import com.google.common.primitives.Primitives;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Class instance creation utility (to gather all instantiations in one place).
 *
 * @author Vyacheslav Rusakov
 * @since 16.11.2023
 */
public final class InstanceUtils {

    private InstanceUtils() {
    }

    /**
     * Create a new instance using a no-args constructor.
     *
     * @param type class
     * @param <T>  instance type
     * @return class instance
     * @see #createWithAnyConstructor(Class) to search for an appropriate constructor and simulate arguments
     */
    public static <T> T create(final Class<T> type) {
        try {
            // getDeclaredConstructor because getConstructors does not returns default constructor
            return type.getDeclaredConstructor().newInstance();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to instantiate class with default constructor: "
                    + type.getName(), ex);
        }
    }

    /**
     * Shortcut for {@link #create(Class, Class[], Object...)} for one constructor argument.
     *
     * @param type  class
     * @param param constructor parameter type
     * @param arg   parameter value
     * @param <T>   instance type
     * @return object instance
     */
    public static <T> T create(final Class<T> type, final Class<?> param, final Object arg) {
        return create(type, new Class[]{param}, arg);
    }

    /**
     * Create a new instance using the constructor with provided parameters and values.
     *
     * @param type  class
     * @param param constructor parameter types
     * @param args  constructor arguments
     * @param <T>   instance type
     * @return object instance
     */
    public static <T> T create(final Class<T> type, final Class<?>[] param, final Object... args) {
        try {
            // only public constructors
            return type.getConstructor(param).newInstance(args);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to instantiate class: " + type.getName()
                    + "\n\t with constructor params: "
                    + Arrays.stream(param).map(Class::getSimpleName).collect(Collectors.joining(", "))
                    + "\n\t and values: "
                    + Arrays.stream(args).map(String::valueOf).collect(Collectors.joining(", ")), ex);
        }
    }

    /**
     * Create new instance using constructor with provided params and nulls as values.
     *
     * @param type  class
     * @param param constructor params
     * @param <T>   instance type
     * @return object instance
     */
    public static <T> T createWithNulls(final Class<T> type, final Class<?>... param) {
        final Object[] args = new Object[param.length];
        return create(type, param, args);
    }

    /**
     * Create new instance using constructor with provided params and dummy values (simulated non-null objects
     * to cheat constructor non-null checks or simple argument object method calls).
     *
     * @param type  class
     * @param param constructor params
     * @param <T>   instance type
     * @return object instance
     */
    public static <T> T createWithDummyArgs(final Class<T> type, final Class<?>... param) {
        final Object[] args = new Object[param.length];
        for (int i = 0; i < param.length; i++) {
            final Class<?> clazz = param[i];
            // whatever non-null (if constructor checks for non-null)
            args[i] = createDummyInstance(clazz);
        }

        return create(type, param, args);
    }

    /**
     * Create a dummy object instance.
     * Supports: primitives, arrays, interfaces, and custom objects.
     * <p>
     * Use in tests or for test-related stuff!
     *
     * @param type type to instantiate
     * @return object instance
     */
    @SuppressWarnings({"checkstyle:CyclomaticComplexity", "PMD.CyclomaticComplexity"})
    public static Object createDummyInstance(final Class<?> type) {
        final Object arg;
        final Class<?> prim = Primitives.unwrap(type);
        if (prim.isPrimitive()) {
            if (boolean.class.equals(prim)) {
                arg = Boolean.FALSE;
            } else if (long.class.equals(prim)) {
                arg = 1L;
            } else if (short.class.equals(prim)) {
                arg = (short) 1;
            } else if (byte.class.equals(prim)) {
                arg = (byte) 1;
            } else if (char.class.equals(prim)) {
                arg = 'a';
            } else if (float.class.equals(prim)) {
                arg = (float) 1;
            } else if (double.class.equals(prim)) {
                arg = (double) 1;
            } else {
                arg = 1;
            }
        } else if (String.class.equals(type)) {
            // not ideal :for example, Integer would fail with this string in constructor
            arg = "dummy";
        } else if (type.isArray()) {
            return Array.newInstance(type.getComponentType(), 0);
        } else if (type.isInterface() || Modifier.isAbstract(type.getModifiers())) {
            arg = ProxyUtils.createDummyProxy(type);
        } else {
            arg = createWithAnyConstructor(type);
        }
        return arg;
    }

    /**
     * For tests ONLY!
     * <p>
     * First tries to use a default constructor, then search for a declared constructor with minimal arguments
     * and try to instantiate with ulls. If nulls not work (constructor calls argument methods or performs non-null
     * validations), try to create dummy objects for arguments.
     *
     * @param type class
     * @param <T>  target type
     * @return class instance
     */
    @SuppressWarnings({"PMD.PreserveStackTrace", "checkstyle:ReturnCount"})
    public static <T> T createWithAnyConstructor(final Class<T> type) {
        try {
            // first try to create with a default constructor
            return create(type);
        } catch (RuntimeException ex) {
            // fallback to constructor with arguments
            final Constructor<T> ctor = findMinimalConstructor(type);
            if (ctor != null) {
                try {
                    // first try with nulls
                    return createWithNulls(type, ctor.getParameterTypes());
                } catch (Exception e) {
                    try {
                        // then try with dummy arguments
                        return createWithDummyArgs(type, ctor.getParameterTypes());
                    } catch (Exception fail) {
                        // human-readable cause
                        throw new IllegalStateException("Failed to create dummy instance of " + type.getName()
                                + " with constructor (" + Arrays.stream(ctor.getParameterTypes())
                                .map(Class::getSimpleName).collect(Collectors.joining(", ")) + ")", fail);
                    }
                }
            } else {
                throw new IllegalStateException("Failed to find suitable constructor for " + type);
            }
        }
    }

    /**
     * Search for a constructor with minimum arguments.
     * Note: if the class does not have declared constructors - null would be returned!
     *
     * @param source source class
     * @param <T>    class type
     * @return constructor with minimum arguments or null for interface or if no constructors declared
     */
    @SuppressWarnings("unchecked")
    public static <T> Constructor<T> findMinimalConstructor(final Class<T> source) {
        Constructor<?> res = null;
        if (!source.isInterface()) {
            // search for constructor with minimum arguments
            // this is not an ideal method - there might be primitive types or non-null arguments might be required
            for (Constructor<?> constructor : source.getDeclaredConstructors()) {
                if (res == null || isSimpler(res, constructor)) {
                    res = constructor;
                }
            }
        }
        return (Constructor<T>) res;
    }

    private static boolean isSimpler(final Constructor<?> source, final Constructor<?> cand) {
        if (source.getParameterCount() == cand.getParameterCount()) {
            // try to compare parameters complexity (constructor with more primitives is simpler)
            return countComplexity(source) > countComplexity(cand);
        }
        return source.getParameterCount() > cand.getParameterCount();
    }

    private static int countComplexity(final Constructor<?> ctor) {
        int complexity = 0;
        // primitive params in priority
        for (Class<?> param : ctor.getParameterTypes()) {
            complexity += Primitives.unwrap(param).isPrimitive() ? 1 : 2;
        }
        return complexity;
    }
}
