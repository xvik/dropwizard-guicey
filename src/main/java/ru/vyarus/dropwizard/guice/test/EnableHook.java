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
 * Target fields must be static fields. If non static or not
 * {@link ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook} filed annotated then error will be thrown indicating
 * incorrect usage.
 * <p>
 * Annotation works with junit 5 and spock extensions (dropwizard and guicey).
 *
 * @author Vyacheslav Rusakov
 * @since 25.05.2020
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface EnableHook {
}
