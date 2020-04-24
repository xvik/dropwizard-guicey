package ru.vyarus.dropwizard.guice.module.installer.scanner;

import java.lang.annotation.*;

/**
 * Used to exclude class from classpath scanning or prevent guice binding recognition as extension.
 * <p>
 * For guice bindings there is a side-effect: if annotated extension will be registered manually, binding
 * will not be recognized and guicey will try to register default binding for extension, which most likely will
 * fail context creation. So it is better to use this annotation only on classes which are not intended to be
 * registered at all. As a workaround, {@link ru.vyarus.dropwizard.guice.module.installer.install.binding.LazyBinding}
 * may be used to avoid default binding creation for extension.
 *
 * @author Vyacheslav Rusakov
 * @since 01.09.2014
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface InvisibleForScanner {
}
