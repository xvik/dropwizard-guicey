package ru.vyarus.dropwizard.guice.module.installer.util;

import java.lang.reflect.Constructor;
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
     * Create a new instance using no-args constructor.
     *
     * @param type class
     * @param <T>  instance type
     * @return class instance
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
     * Create a new instance using constructor with provided parameters and values.
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
        Arrays.fill(args, null);
        return create(type, param, args);
    }

    /**
     * For tests ONLY!
     * <p>
     * Tries to find constructor with the smallest amount of arguments and use it with nulls to instantiate object.
     * <p>
     * WARNING: primitive arguments are not supported!
     *
     * @param type class
     * @param <T>  target type
     * @return class instance
     */
    @SuppressWarnings("PMD.PreserveStackTrace")
    public static <T> T createWithAnyConstructor(final Class<T> type) {
        try {
            return create(type);
        } catch (RuntimeException ex) {
            // fallback to any constructor with null arguments
            Constructor cand = null;
            for (Constructor ctor : type.getDeclaredConstructors()) {
                if (cand == null || cand.getParameterCount() > ctor.getParameterCount()) {
                    cand = ctor;
                }
            }
            if (cand != null) {
                return createWithNulls(type, cand.getParameterTypes());
            } else {
                throw new IllegalStateException("Failed to find suitable constructor for " + type);
            }
        }
    }
}
