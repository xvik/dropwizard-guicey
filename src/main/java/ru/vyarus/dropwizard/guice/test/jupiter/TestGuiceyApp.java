package ru.vyarus.dropwizard.guice.test.jupiter;

import io.dropwizard.Application;
import org.junit.jupiter.api.extension.ExtendWith;
import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.TestGuiceyAppExtension;
import ru.vyarus.dropwizard.guice.test.jupiter.param.ClientSupport;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Guicey app junit 5 extension. Runs only guice context without starting web part (jetty and jersey).
 * Useful for testing core business services.
 * <p>
 * Note: in spite of the fact that jersey not started, {@link io.dropwizard.lifecycle.Managed} objects would
 * be started and stopped normally.
 * <p>
 * Extension may be declared on test class or on any nested class. When declared on nested class, applies to
 * all lower nested classes (default junit 5 extension appliance behaviour). Multiple extension declarations
 * (on multiple nested levels) is useless as junit will use only the top declared extension instance
 * (and other will be ignored).
 * <p>
 * Guice context started before all tests (before {@link org.junit.jupiter.api.BeforeAll}) and shut down
 * after all tests (after {@link org.junit.jupiter.api.AfterAll}). There is no way to restart it between tests. Group
 * test, not modifying internal application state and extract test modifying state to separate classes (possibly
 * junit nested tests).
 * <p>
 * Guice injections will work on test fields annotated with {@link javax.inject.Inject} or
 * {@link com.google.inject.Inject} ({@link com.google.inject.Injector#injectMembers(Object)} applied on test instance).
 * Guice AOP will not work on test methods (because test itself is not created by guice).
 * <p>
 * Test constructor, lifecycle and test methods may use additional parameters:
 * <ul>
 *     <li>Any declared (unqualified) guice bean</li>
 *     <li>For not declared beans use {@link ru.vyarus.dropwizard.guice.test.jupiter.param.Jit} to force JIT
 *     binding (create declared bean with guice, even if it wasn't registered)</li>
 *     <li>Specially supported objects:
 *     <ul>
 *         <li>{@link Application} or exact application class</li>
 *         <li>{@link io.dropwizard.Configuration} or exact configuration class</li>
 *         <li>{@link io.dropwizard.setup.Environment}</li>
 *         <li>{@link com.fasterxml.jackson.databind.ObjectMapper}</li>
 *         <li>{@link com.google.inject.Injector}</li>
 *         <li>{@link ClientSupport} for calling external urls</li>
 *     </ul>
 *     </li>
 * </ul>
 * <p>
 * Internally use {@link io.dropwizard.testing.DropwizardTestSupport} with custom command
 * ({@link ru.vyarus.dropwizard.guice.test.TestCommand}).
 * <p>
 * It is possible to apply extension manually using {@link org.junit.jupiter.api.extension.RegisterExtension}
 * and {@link TestGuiceyAppExtension#forApp(Class)} builder. The only difference is declaration type, but in both cases
 * extension will work the same way.
 *
 * @author Vyacheslav Rusakov
 * @since 29.04.2020
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@ExtendWith(TestGuiceyAppExtension.class)
@Inherited
public @interface TestGuiceyApp {

    /**
     * @return application class
     */
    Class<? extends Application> value();

    /**
     * @return path to configuration file (optional)
     */
    String config() default "";

    /**
     * Each value must be written as {@code key=value}.
     *
     * @return list of overridden configuration values (may be used even without real configuration)
     */
    String[] configOverride() default {};

    /**
     * Hooks provide access to guice builder allowing complete customization of application context
     * in tests.
     *
     * @return list of hooks to use
     * @see GuiceyConfigurationHook for more info
     */
    Class<? extends GuiceyConfigurationHook>[] hooks() default {};
}
