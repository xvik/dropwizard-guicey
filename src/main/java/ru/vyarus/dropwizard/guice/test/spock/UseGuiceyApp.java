package ru.vyarus.dropwizard.guice.test.spock;

import io.dropwizard.Application;
import org.spockframework.runtime.extension.ExtensionAnnotation;
import ru.vyarus.dropwizard.guice.configurator.GuiceyConfigurator;
import ru.vyarus.dropwizard.guice.test.spock.ext.GuiceyAppExtension;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Guicey app extension. Works almost the same as {@link ru.vyarus.dropwizard.guice.test.GuiceyAppRule}, but
 * application instance is created for all tests in class (as if rule would be used with @ClassRule annotation)
 * <p>Services will be injected into the specification based on regular Guice annotations. {@code @Share} may
 * be used to define common injection points for all tests in class.</p>
 * <p>Note: {@code setupSpec()} fixture is called after application start and {@code cleanupSpec()} before
 * application tear down.</p>
 * <p>Extension behaviour is the same as spock-guice module.</p>
 *
 * @author Vyacheslav Rusakov
 * @see ru.vyarus.dropwizard.guice.test.GuiceyAppRule for details
 * @since 02.01.2015
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@ExtensionAnnotation(GuiceyAppExtension.class)
public @interface UseGuiceyApp {

    /**
     * @return application class
     */
    Class<? extends Application> value();

    /**
     * @return path to configuration file (optional)
     */
    String config() default "";

    /**
     * @return list of overridden configuration values (may be used even without real configuration)
     */
    ConfigOverride[] configOverride() default {};

    /**
     * Configurators provide access to guice builder allowing complete customization of application context
     * in tests.
     *
     * @return list of configurators to use
     * @see UseGuiceyConfigurator to declare base configurators in base test class
     * @see GuiceyConfigurator for more info
     */
    Class<? extends GuiceyConfigurator>[] configurators() default {};
}
