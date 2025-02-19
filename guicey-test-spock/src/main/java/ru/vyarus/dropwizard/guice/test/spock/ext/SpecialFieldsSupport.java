package ru.vyarus.dropwizard.guice.test.spock.ext;

import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook;
import ru.vyarus.dropwizard.guice.test.ClientSupport;
import ru.vyarus.dropwizard.guice.test.EnableHook;
import ru.vyarus.dropwizard.guice.test.spock.InjectClient;
import ru.vyarus.dropwizard.guice.test.jupiter.env.field.AnnotatedField;
import ru.vyarus.dropwizard.guice.test.jupiter.env.field.TestFieldUtils;
import spock.lang.Shared;

import java.util.ArrayList;
import java.util.List;

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
        final List<AnnotatedField<EnableHook, GuiceyConfigurationHook>> fields = TestFieldUtils.findAnnotatedFields(
                test, EnableHook.class, GuiceyConfigurationHook.class);
        fields.forEach(AnnotatedField::requireStatic);
        for (GuiceyConfigurationHook hook : TestFieldUtils.getValues(fields, null)) {
            if (hook != null) {
                hooks.add(hook);
            }
        }
        return hooks;
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
                                   final List<AnnotatedField<InjectClient, ClientSupport>> fields,
                                   final ClientSupport client,
                                   final boolean shared) {
        fields.forEach(field -> {
            // skip instance injections for static and static injection for instance
            final boolean incompatibleField = (instance == null && !field.isStatic())
                    || (instance != null && field.isStatic());
            if (field.getField().isAnnotationPresent(Shared.class) != shared || incompatibleField) {
                return;
            }
            field.setValue(instance, client);
        });
    }
}

