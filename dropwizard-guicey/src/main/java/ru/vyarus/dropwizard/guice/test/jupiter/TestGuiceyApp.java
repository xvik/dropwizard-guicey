package ru.vyarus.dropwizard.guice.test.jupiter;

import io.dropwizard.core.Application;
import org.junit.jupiter.api.extension.ExtendWith;
import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook;
import ru.vyarus.dropwizard.guice.test.ClientSupport;
import ru.vyarus.dropwizard.guice.test.jupiter.env.TestEnvironmentSetup;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.TestGuiceyAppExtension;
import ru.vyarus.dropwizard.guice.test.util.client.DefaultTestClientFactory;
import ru.vyarus.dropwizard.guice.test.util.client.TestClientFactory;

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
 * after all tests (after {@link org.junit.jupiter.api.AfterAll}). If you need to restart application between tests
 * then declare extension in {@link org.junit.jupiter.api.extension.RegisterExtension} non-static field instead of
 * annotation.
 * <p>
 * Guice injections will work on test fields annotated with {@link jakarta.inject.Inject} or
 * {@link com.google.inject.Inject} ({@link com.google.inject.Injector#injectMembers(Object)} applied on test instance).
 * Guice AOP will not work on test methods (because test itself is not created by guice).
 * <p>
 * Test constructor, lifecycle and test methods may use additional parameters:
 * <ul>
 *     <li>Any declared (possibly with qualifier annotation or generified) guice bean</li>
 *     <li>For not declared beans use {@link ru.vyarus.dropwizard.guice.test.jupiter.param.Jit} to force JIT
 *     binding (create declared bean with guice, even if it wasn't registered)</li>
 *     <li>Specially supported objects:
 *     <ul>
 *         <li>{@link Application} or exact application class</li>
 *         <li>{@link com.fasterxml.jackson.databind.ObjectMapper}</li>
 *         <li>{@link ClientSupport} for calling external urls</li>
 *     </ul>
 *     </li>
 * </ul>
 * <p>
 * Internally use {@link io.dropwizard.testing.DropwizardTestSupport} with custom command
 * ({@link ru.vyarus.dropwizard.guice.test.TestCommand}).
 * <p>
 * It is possible to apply extension manually using {@link org.junit.jupiter.api.extension.RegisterExtension}
 * and {@link TestGuiceyAppExtension#forApp(Class)} builder. For static field the behaviour would be the same as with
 * annotation, for non-static field application will restart before each test method.
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
     * Each value must be written as {@code key: value}.
     * <p>
     * In order to specify raw {@link io.dropwizard.testing.ConfigOverride} values (for delayed evaluation)
     * use direct extension registration with {@link org.junit.jupiter.api.extension.RegisterExtension} instead
     * of annotation.
     *
     * @return list of overridden configuration values (may be used even without real configuration)
     */
    String[] configOverride() default {};

    /**
     * Hooks provide access to guice builder allowing complete customization of application context
     * in tests.
     * <p>
     * For anonymous hooks you can simply declare hook as field:
     * {@code @EnableHook static GuiceyConfigurationHook hook = builder -> builder.disableExtension(Something.class)}.
     * Non-static fields may be used only when extension is registered with non-static field (static fields would be
     * also counted in this case). All annotated fields will be detected automatically and objects registered. Fields
     * declared in base test classes are also counted.
     *
     * @return list of hooks to use
     * @see GuiceyConfigurationHook for more info
     * @see ru.vyarus.dropwizard.guice.test.EnableHook
     */
    Class<? extends GuiceyConfigurationHook>[] hooks() default {};

    /**
     * Environment support object is the simplest way to prepare additional objects for test
     * (like database) and apply configuration overrides. Provided classes would be instantiated with the
     * default constructor.
     * <p>
     * To avoid confusion with guicey hooks: setup object required to prepare test environment before test (and apply
     * required configurations) whereas hooks is a general mechanism for application customization (not only in tests).
     * <p>
     * Anonymous implementation could be simply declared as field:
     * {@code @EnableSetup static TestEnvironmentSetup ext = ext -> ext.configOverrides("foo:1")}.
     * Non-static fields may be used only when extension is registered with non-static field (static fields would be
     * also counted in this case). All annotated fields will be detected automatically and objects registered. Fields
     * declared in base test classes are also counted.
     *
     * @return setup objects to use
     * @see ru.vyarus.dropwizard.guice.test.jupiter.env.EnableSetup
     */
    Class<? extends TestEnvironmentSetup>[] setup() default {};

    /**
     * Enables debug output for extension: used setup objects, hooks and applied config overrides. Might be useful
     * for concurrent tests too because each message includes configuration prefix (exactly pointing to context test
     * or method).
     * <p>
     * Configuration overrides are printed after application startup (but before the test) because overridden values
     * are resolved from system properties (applied by {@link io.dropwizard.testing.DropwizardTestSupport#before()}).
     * If application startup failed, no configuration overrides would be printed (because dropwizard would immediately
     * cleanup system properties). Using system properties is the only way to receive actually applied configuration
     * value because property overrides might be implemented as value providers and potentially return different values.
     * <p>
     * System property might be used to enable debug mode: {@code -Dguicey.extensions.debug=true}. Or alias in code:
     * {@link ru.vyarus.dropwizard.guice.test.TestSupport#debugExtensions()}.
     *
     * @return true to enable debug output, false otherwise
     */
    boolean debug() default false;

    /**
     * By default, new application instance is started for each test. If you want to re-use the same application
     * instance between several tests then declare application in BASE test class and enable reuse option: all tests,
     * derived from this base class would use the same application instance.
     * <p>
     * You may have multiple base classes with reusable application declaration (different test hierarchies) - in
     * this case, multiple applications would be kept running during tests execution.
     * <p>
     * All other extensions (without enabled re-use) will start new applications: take this into account to
     * prevent port clashes with already started reusable apps.
     * <p>
     * Reused application instance would be stopped after all tests execution.
     *
     * @return true to reuse application, false to start application for each test
     */
    boolean reuseApplication() default false;

    /**
     * Custom client factory for {@link ru.vyarus.dropwizard.guice.test.ClientSupport} object. Custom factory
     * may be required in case when custom client configuration is required for test.
     * <p>
     * Core test does not start dropwizard web services, but client still could be used to call external services.
     *
     * @return client factory class
     */
    Class<? extends TestClientFactory> clientFactory() default DefaultTestClientFactory.class;
}
