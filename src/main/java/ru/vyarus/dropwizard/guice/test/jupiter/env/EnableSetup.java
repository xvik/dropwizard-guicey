package ru.vyarus.dropwizard.guice.test.jupiter.env;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker annotation for {@link ru.vyarus.dropwizard.guice.test.jupiter.env.TestEnvironmentSetup} test fields
 * declaration. Extra annotation required to remove uncertainty and apply some context (avoid questions why it works).
 * <p>
 * Example usage: <pre>{@code @EnableSetup static TestEnvironmentSetup EXT = ext -> {
 *  Something smth = new Something();
 *  smth.start()
 *  ext.configOverride("value", () -> smth.getValue());
 *  // assume Something implements Closable and so would be closed after test
 *  return smth;
 * }}</pre>
 * <p>
 * Target fields must be static fields. If non static or not
 * {@link ru.vyarus.dropwizard.guice.test.jupiter.env.TestEnvironmentSetup} filed annotated then error will be thrown
 * indicating incorrect usage.
 *
 * @author Vyacheslav Rusakov
 * @see ru.vyarus.dropwizard.guice.test.EnableHook
 * @since 14.05.2022
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface EnableSetup {
}
