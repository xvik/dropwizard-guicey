package ru.vyarus.dropwizard.guice.test.spock;

import org.spockframework.runtime.extension.ExtensionAnnotation;
import ru.vyarus.dropwizard.guice.configurator.GuiceyConfigurator;
import ru.vyarus.dropwizard.guice.test.spock.ext.GuiceyConfiguratorExtension;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Guicey configurator extension. Used to register {@link GuiceyConfigurator} in base test class (usually
 * with debug  extensions, common for all tests). In actual tests use {@code configurators} attribute of
 * {@link UseGuiceyApp} or {@link UseDropwizardApp} extensions to apply test-specific configurations.
 * <p>
 * WARNING: only one {@link UseGuiceyConfigurator} annotation could be used in test hierarchy. For example,
 * you can't use it in both base class and test class. This is spock limitation, but should not be an issue for
 * most cases.
 *
 * @author Vyacheslav Rusakov
 * @since 12.04.2018
 * @see GuiceyConfigurator for more info
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@ExtensionAnnotation(GuiceyConfiguratorExtension.class)
public @interface UseGuiceyConfigurator {

    /**
     * @return list of configurators to use
     */
    Class<? extends GuiceyConfigurator>[] value();
}
