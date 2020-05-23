package ru.vyarus.dropwizard.guice.test.spock.ext;

import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook;
import ru.vyarus.dropwizard.guice.test.ClientSupport;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Support for special test fields. Injects {@link ru.vyarus.dropwizard.guice.test.ClientSupport} and accepts
 * {@link ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook} fields.
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
        for (Field field : findFields(test, GuiceyConfigurationHook.class)) {
            field.setAccessible(true);
            final GuiceyConfigurationHook hook = getValue(field);
            if (hook != null) {
                hooks.add(hook);
            }
        }
        return hooks;
    }

    /**
     * Injects client object into static test fields (including super class).
     *
     * @param test   test class
     * @param client client instance
     */
    public static void initClients(final Class<?> test, final ClientSupport client) {
        for (Field field : findFields(test, ClientSupport.class)) {
            field.setAccessible(true);
            try {
                field.set(null, client);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Failed to set static field " + formatField(field), e);
            }
        }
    }

    private static List<Field> findFields(final Class<?> src, final Class<?> searchFor) {
        final List<Field> res = new ArrayList<>();
        Class<?> cls = src;
        while (cls != Object.class && cls != null) {
            for (Field fld : cls.getDeclaredFields()) {
                if (Modifier.isStatic(fld.getModifiers()) && searchFor.equals(fld.getType())) {
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
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Failed to access static field " + formatField(field), e);
        }
    }

    private static String formatField(final Field field) {
        return field.getDeclaringClass().getName() + "." + field.getName() + " value";
    }
}

