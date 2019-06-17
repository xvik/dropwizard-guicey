package ru.vyarus.dropwizard.guice.module.installer.feature.jersey;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation could be used to mark jersey related extension as managed by guice. By default, all
 * extensions are already managed by guice and so annotation is useless. But when hk-first mode enabled
 * (see {@link ru.vyarus.dropwizard.guice.module.installer.InstallersOptions#JerseyExtensionsManagedByGuice}) then
 * annotation could be used to mark exceptional beans which still must be managed by guice.
 *
 * @author Vyacheslav Rusakov
 * @see JerseyManaged
 * @since 28.04.2018
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface GuiceManaged {
}
