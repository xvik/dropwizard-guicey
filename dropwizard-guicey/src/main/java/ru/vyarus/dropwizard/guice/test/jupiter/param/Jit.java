package ru.vyarus.dropwizard.guice.test.jupiter.param;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker annotation for junit 5 test parameter injection. Required when service not explicitly declared in guice
 * and so can't be recognized directly by type. The annotated parameter will be requested as guice JIT binding:
 * {@code injector.getInstance(ParamType)}.
 * <p>
 * Annotation name reference guice "Just in time" binding type.
 * <p>
 * Could be used with {@link ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp} or
 * {@link ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp} extensions.
 *
 * @author Vyacheslav Rusakov
 * @since 01.05.2020
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Jit {
}
