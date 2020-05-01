package ru.vyarus.dropwizard.guice.test.util;

import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook;

/**
 * Guice hooks test utilities.
 *
 * @author Vyacheslav Rusakov
 * @since 02.05.2020
 */
public final class HooksUtil {

    private HooksUtil() {
    }

    /**
     * Register guciey hooks declared with class only.
     *
     * @param hooks hooks to register
     */
    @SafeVarargs
    public static void register(final Class<? extends GuiceyConfigurationHook>... hooks) {
        for (Class<? extends GuiceyConfigurationHook> hook : hooks) {
            try {
                hook.newInstance().register();
            } catch (Exception e) {
                throw new IllegalStateException("Failed to instantiate guicey hook: " + hook.getSimpleName(), e);
            }
        }
    }
}
