package ru.vyarus.dropwizard.guice.test.jupiter;

import io.dropwizard.core.Application;
import org.junit.jupiter.api.extension.ExtendWith;
import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook;
import ru.vyarus.dropwizard.guice.test.jupiter.env.TestEnvironmentSetup;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.TestDropwizardAppExtension;
import ru.vyarus.dropwizard.guice.test.client.DefaultTestClientFactory;
import ru.vyarus.dropwizard.guice.test.client.TestClientFactory;
import ru.vyarus.dropwizard.guice.test.util.ConfigModifier;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Dropwizard app junit 5 extension. Runs complete dropwizard application. Useful for testing web endpoints.
 * <p>
 * Extension may be declared on test class or on any nested class. When declared on nested class, applies to
 * all lower nested classes (default junit 5 extension appliance behaviour). Multiple extension declarations
 * (on multiple nested levels) is useless as junit will use only the top declared extension instance
 * (and other will be ignored).
 * <p>
 * Application started before all tests (before {@link org.junit.jupiter.api.BeforeAll}) and shut down
 * after all tests (after {@link org.junit.jupiter.api.AfterAll}). If you need to restart application between tests
 * then declare extension in {@link org.junit.jupiter.api.extension.RegisterExtension} non-static field instead of
 * annotation.
 * <p>
 * Guice injections will work on test fields annotated with {@link javax.inject.Inject} or
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
 *         <li>{@link ru.vyarus.dropwizard.guice.test.ClientSupport} for calling application web
 *         endpoints (or external urls). Also it provides actual application ports.</li>
 *     </ul>
 *     </li>
 * </ul>
 * <p>
 * Internally use {@link io.dropwizard.testing.DropwizardTestSupport}.
 * <p>
 * It is possible to apply extension manually using {@link org.junit.jupiter.api.extension.RegisterExtension}
 * and {@link TestDropwizardAppExtension#forApp(Class)} builder. For static field the behaviour would be the same as
 * with annotation, for non-static field application will restart before each test method.
 *
 * @author Vyacheslav Rusakov
 * @since 28.04.2020
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@ExtendWith(TestDropwizardAppExtension.class)
@Inherited
public @interface TestDropwizardApp {

    /**
     * @return application class
     */
    Class<? extends Application<?>> value();

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
     * @see #configModifiers()
     */
    String[] configOverride() default {};

    /**
     * Configuration modifier is an alternative for configuration override, which is limited for simple
     * property types (for example, a collection could not be overridden).
     *
     * @return configuration modifiers
     */
    Class<? extends ConfigModifier<?>>[] configModifiers() default {};

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
     * Enables random ports usage. Supports both simple and default dropwizard servers. Random ports would be
     * set even if you specify exact configuration file with configured ports (option overrides configuration).
     * <p>
     * To get port numbers in test use {@link ru.vyarus.dropwizard.guice.test.ClientSupport} parameter
     * in lifecycle or test method:
     * <pre>{@code
     * static beforeAll(ClientSupport client) {
     *     String baseUrl = "http://localhost:" + client.getPort();
     *     String baseAdminUrl = "http://localhost:" + client.getAdminPort();
     * }
     * }</pre>
     * Or use client target methods directly.
     *
     * @return true to use random ports
     */
    boolean randomPorts() default false;

    /**
     * Specifies rest mapping path. This is the same as specifying {@link #configOverride()}
     * {@code "server.rootMapping=/something/*"}. Specified value would be prefixed with "/" and, if required
     * "/*" applied at the end. So it would be correct to specify {@code restMapping = "api"} (actually set value
     * would be "/api/*").
     * <p>
     * This option is only intended to simplify cases when custom configuration file is not yet used in tests
     * (usually early PoC phase). It allows you to map servlet into application root in test (because rest is no
     * more resides in root). When used with existing configuration file, this parameter will override file definition.
     *
     * @return rest mapping (empty string - do nothing)
     */
    String restMapping() default "";

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
     * When test lifecycle is {@link org.junit.jupiter.api.TestInstance.Lifecycle#PER_CLASS} same test instance
     * used for all test methods. By default, guicey would perform fields injection before each method because
     * there might be prototype beans that must be refreshed for each test method. If you don't rely on
     * prototypes, injections could be performed just once (for the first test method).
     *
     * @return true to inject guice beans once per test instance, false otherwise
     */
    boolean injectOnce() default false;

    /**
     * Enables debug output for extension: used setup objects, hooks and applied config overrides. Might be useful
     * for concurrent tests too because each message includes configuration prefix (exactly pointing to context test
     * or method).
     * <p>
     * Also, shows guicey extension time, so if you suspect that guicey spent too much time, use the debug option to
     * be sure. Performance report is published after each "before each" phase and after "after all" to let you
     * see how extension time increased with each test method (for non-static guicey extension (executed per method),
     * performance printed after "before each" and "after each" because before/after all not available)
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
     * By default, a new application instance is started for each test. If you want to re-use the same application
     * instance between several tests, then put extension declaration in BASE test class and enable the reuse option:
     * all tests derived from this base class would use the same application instance.
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
     * Default extensions: {@link ru.vyarus.dropwizard.guice.test.jupiter.ext.mock.MockBean},
     * {@link ru.vyarus.dropwizard.guice.test.jupiter.ext.stub.StubBean},
     * {@link ru.vyarus.dropwizard.guice.test.jupiter.ext.spy.SpyBean},
     * {@link ru.vyarus.dropwizard.guice.test.jupiter.ext.track.TrackBean},
     * {@link ru.vyarus.dropwizard.guice.test.jupiter.ext.log.RecordLogs},
     * {@link ru.vyarus.dropwizard.guice.test.jupiter.ext.rest.StubRest}.
     * <p>
     * Disables service lookup for {@link ru.vyarus.dropwizard.guice.test.jupiter.env.TestEnvironmentSetup}.
     * <p>
     * By default, these extensions enabled and this option could disable them (if there are problems with them or
     * fields analysis took too much time).
     *
     * @return true to use default extensions
     */
    boolean useDefaultExtensions() default true;

    /**
     * Custom client factory for {@link ru.vyarus.dropwizard.guice.test.ClientSupport} object. Custom factory
     * may be required in case when custom client configuration is required for test.
     * <p>
     * Note: value is ignored when {@link #apacheClient()} set to true
     *
     * @return client factory class
     */
    Class<? extends TestClientFactory> clientFactory() default DefaultTestClientFactory.class;

    /**
     * Shortcut for {@link #clientFactory()} to configure
     * {@link ru.vyarus.dropwizard.guice.test.client.ApacheTestClientFactory}. The default
     * {@link org.glassfish.jersey.client.HttpUrlConnectorProvider} supports only HTTP 1.1 methods and have
     * problem with PATCH method usage on jdk &gt; 16.
     * <p>
     * Note: {@link #clientFactory()} value is ignored when set to true,
     * <p>
     * Apache client is not set by default because of its problems with multipart: see
     * <a href="https://github.com/eclipse-ee4j/jersey/issues/5528#issuecomment-1934766714">jersey issue</a>.
     *
     * @return true to use apache connection provider in jersey client
     */
    boolean apacheClient() default false;
}
