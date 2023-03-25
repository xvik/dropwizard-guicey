package ru.vyarus.dropwizard.guice.test.spock;

import io.dropwizard.core.Application;
import org.spockframework.runtime.extension.ExtensionAnnotation;
import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook;
import ru.vyarus.dropwizard.guice.test.spock.ext.GuiceyAppExtension;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Guicey spock extension. Starts guice context only (without web part). {@link io.dropwizard.lifecycle.Managed}
 * objects will be still executed correctly. Guice injectior is created before all tests in class and shut down
 * after them.
 * <p>
 * Gucie injections will work on test class (just annotate required fields with {@link javax.inject.Inject}.
 * {@link spock.lang.Shared} may be used to define common injection points for all tests in class.
 * <p>
 * Note: {@code setupSpec()} fixture is called after application start and {@code cleanupSpec()} before
 * application tear down.
 * <p>
 * Extension would also recognize the following test fields (including super classes):
 * <ul>
 * <li>static {@link GuiceyConfigurationHook} annotated with {@link ru.vyarus.dropwizard.guice.test.EnableHook} - hook
 * from field will be registered</li>
 * <li>{@link ru.vyarus.dropwizard.guice.test.ClientSupport} annotated with {@link InjectClient} field will be injected
 * with client instance. Note that only generic client may be used (to call 3rd party external services), as
 * application's web part is not started.</li>
 * </ul>
 * <p>
 * Internally based on {@link io.dropwizard.testing.DropwizardTestSupport}.
 *
 * @author Vyacheslav Rusakov
 * @since 02.01.2015
 * @see InjectClient
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
     * Hooks provide access to guice builder allowing complete customization of application context
     * in tests.
     * <p>
     * Additional hooks could be declared in static test fields:
     * {@code @EnableHook static GuiceyConfigurationHook HOOK = { it.disableExtensions(Something.class)}}.
     *
     * @return list of hooks to use
     * @see GuiceyConfigurationHook for more info
     * @see ru.vyarus.dropwizard.guice.test.EnableHook
     */
    Class<? extends GuiceyConfigurationHook>[] hooks() default {};
}
