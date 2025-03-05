package ru.vyarus.dropwizard.guice.test.util;

import io.dropwizard.core.Configuration;

/**
 * Configuration modifier is an alternative for configuration override, which is limited for simple
 * property types (for example, a collection could not be overridden).
 * <p>
 * Modifier is called before application run phase. Only logger configuration is applied at this moment (and so you
 * can't change it). Modifier would work with both yaml and instance-based configurations.
 * <p>
 * Could be declared in:
 *  - {@link ru.vyarus.dropwizard.guice.test.jupiter.ext.TestGuiceyAppExtension.Builder#configModifiers(
 *  ConfigModifier[])} extension builder (same for dropwizard extension)
 *  - {@link ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp#configModifiers()} extension annotation (same for
 *  dropwizard annotation)
 *  - {@link ru.vyarus.dropwizard.guice.test.TestSupport#build(Class)} - generic test support object builder
 *  - {@link ru.vyarus.dropwizard.guice.test.TestSupport#buildCommandRunner(Class)} - generic test command runner
 *  - {@link ru.vyarus.dropwizard.guice.test.GuiceyTestSupport#configModifiers(ConfigModifier[])} - guicey support
 *  object (directly)
 *  - {@link ru.vyarus.dropwizard.guice.test.cmd.CommandTestSupport#configModifiers(ConfigModifier[])} - command
 *  support object (directly)
 *  <p>
 *  To enable support for {@link io.dropwizard.testing.DropwizardTestSupport} custom command factory must be used:
 *  {@link ru.vyarus.dropwizard.guice.test.util.ConfigOverrideUtils#buildCommandFactory(java.util.List)}.
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
