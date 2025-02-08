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
 * Field might be not static only if extension is registered in non-static field (application started for each test
 * method), otherwise it must be static. Incorrect usage will be indicated with an exception.
 * <p>
 * If setup object implements hook ({@link ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook})
 * and/or listener ({@link ru.vyarus.dropwizard.guice.test.jupiter.env.TestExecutionListener}) it would be
 * registered automatically (no need for manual registration). Manual registration would not create duplicate.
 *
 * @author Vyacheslav Rusakov
 * @see ru.vyarus.dropwizard.guice.test.EnableHook
 * @since 14.05.2022
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface EnableSetup {
}
