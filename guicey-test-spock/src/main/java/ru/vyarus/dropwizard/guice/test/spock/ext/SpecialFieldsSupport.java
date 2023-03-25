package ru.vyarus.dropwizard.guice.test.spock.ext;

import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook;
import ru.vyarus.dropwizard.guice.test.ClientSupport;
import ru.vyarus.dropwizard.guice.test.EnableHook;
import ru.vyarus.dropwizard.guice.test.spock.InjectClient;
import ru.vyarus.dropwizard.guice.test.util.HooksUtil;
import spock.lang.Shared;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Support for special test fields. Injects {@link ClientSupport} and accepts
 * {@link GuiceyConfigurationHook} fields.
 *
 * @author Vyacheslav Rusakov
 * @since 17.05.2020
 */
public final class SpecialFieldsSupport {

    private SpecialFieldsSupport() {
    }

    /**
     * Search guicey hooks in static test fields (including super classes).
     *
     * @param test test class
     * @return list of found hook objects or empty list
     */
    public static List<GuiceyConfigurationHook> findHooks(final Class<?> test) {
        final List<GuiceyConfigurationHook> hooks = new ArrayList<>();
        final List<Field> fields = findFields(test, field -> field.isAnnotationPresent(EnableHook.class));
        HooksUtil.validateFieldHooks(fields, false);
        for (Field field : fields) {
            field.setAccessible(true);
            final GuiceyConfigurationHook hook = getValue(field);
            if (hook != null) {
                hooks.add(hook);
            }
        }
        return hooks;
    }

    /**
     * @param test test class
     * @return all fields annotated with {@link InjectClient} or empty list
     */
    public static List<Field> findClientFields(final Class<?> test) {
        final List<Field> fields = findFields(test, it -> it.isAnnotationPresent(InjectClient.class));
        for (Field field : fields) {
            if (!field.getType().equals(ClientSupport.class)) {
                throw new IllegalStateException(String.format(
                        "Field %s annotated with @%s, but its type is not %s",
                        toString(field), InjectClient.class.getSimpleName(), ClientSupport.class.getSimpleName()
                ));
            }
        }
        return fields;
    }

    /**
     * Injects client object into static test fields (including super class).
     *
     * @param instance test instance (null for static injection)
     * @param fields   all client fields (static, shared, instance)
     * @param client   client instance
     * @param shared   process shared fields
     */
    public static void initClients(final Object instance,
                                   final List<Field> fields,
                                   final ClientSupport client,
                                   final boolean shared) {
        for (Field field : fields) {
            if (field.isAnnotationPresent(Shared.class) != shared) {
                continue;
            }
            // skip instance injections for static and static injection for instance
            if ((instance == null && !Modifier.isStatic(field.getModifiers()))
                    || (instance != null && Modifier.isStatic(field.getModifiers()))) {
                continue;
            }
            field.setAccessible(true);
            try {
                field.set(instance, client);
            } catch (Exception e) {
                throw new IllegalStateException("Failed to set value to field " + toString(field), e);
            }
        }
    }

    private static List<Field> findFields(final Class<?> src, final Predicate<Field> predicate) {
        final List<Field> res = new ArrayList<>();
        Class<?> cls = src;
        while (cls != Object.class && cls != null) {
            for (Field fld : cls.getDeclaredFields()) {
                if (predicate.test(fld)) {
                    res.add(fld);
                }
            }
            cls = cls.getSuperclass();
        }
        return res;
    }

    @SuppressWarnings("unchecked")
    private static <T> T getValue(final Field field) {
        try {
            return (T) field.get(null);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to access static field value " + toString(field), e);
        }
    }

    private static String toString(final Field field) {
        return field.getDeclaringClass().getName() + "." + field.getName();
    }
}

