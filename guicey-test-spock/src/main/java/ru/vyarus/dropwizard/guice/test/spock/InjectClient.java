package ru.vyarus.dropwizard.guice.test.spock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker annotation for {@link ru.vyarus.dropwizard.guice.test.ClientSupport} spock test field. Extra annotation
 * required to remove uncertainty and apply some context (avoid confusion why it works).
 * Note that such annotation is not required for junit 5 version because there client may be injected as parameter
 * (so this is only a special fix for spock tests to support the same client object).
 * <p>
 * Example usage: {@code @InjectClient ClientSupport client}
 * <p>
 * Must be used on static, shared or regular fields. When used on not
 * {@link ru.vyarus.dropwizard.guice.test.ClientSupport} field, error will be thrown.
 *
 * @author Vyacheslav Rusakov
 * @since 26.05.2020
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface InjectClient {
}
