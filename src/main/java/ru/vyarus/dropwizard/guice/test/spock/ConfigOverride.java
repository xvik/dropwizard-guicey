package ru.vyarus.dropwizard.guice.test.spock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Must be used together with {@code @UseGuiceApp} or {@code UseDropwizardApp} to specify configuration overrides.
 *
 * @see io.dropwizard.testing.junit.ConfigOverride
 * @author Vyacheslav Rusakov
 * @since 03.01.2015
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ConfigOverride {

    /**
     * @return configuration key
     */
    String key();

    /**
     * @return configuration key value
     */
    String value();
}
