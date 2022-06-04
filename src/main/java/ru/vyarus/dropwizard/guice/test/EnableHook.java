package ru.vyarus.dropwizard.guice.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker annotation for {@link ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook} test fields declaration.
 * Extra annotation required to remove uncertainty and apply some context (avoid confusion why it works).
 * <p>
 * Example usage: {@code @EnableHook static GuiceyConfigurationHook HOOK = builder -> builder.modules(new Mod()) }
 * <p>
 * Field might be not static only if extension is registered in non-static field (application started for each test
 * method), otherwise it must be static. Incorrect usage will be indicated with an exception.
 *
 * @author Vyacheslav Rusakov
 * @see ru.vyarus.dropwizard.guice.test.jupiter.env.EnableSetup
 * @since 25.05.2020
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface EnableHook {
}
