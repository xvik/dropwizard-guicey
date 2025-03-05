package ru.vyarus.dropwizard.guice.test.util;

import io.dropwizard.core.Configuration;

/**
 * Configuration modifier is an alternative for configuration override, which is limited for simple
 * property types (for example, a collection could not be overridden).
 * <p>
 * Modifier is called before application run phase. Only logger configuration is applied at this moment (and so you
 * can't change it). Modifier would work with both yaml and instance-based configurations.
 *
 * @param <C> configuration type
 * @author Vyacheslav Rusakov
 * @since 04.03.2025
 */
@FunctionalInterface
public interface ConfigModifier<C extends Configuration> {

    /**
     * Called before application run phase. Only logger configuration is applied at this moment (and so you
     * can't change it). Modifier would work with both yaml and instance-based configurations.
     *
     * @param config configuration instance
     * @throws Exception on error (to avoid try-catch blocks in modifier itself)
     */
    void modify(C config) throws Exception;
}
