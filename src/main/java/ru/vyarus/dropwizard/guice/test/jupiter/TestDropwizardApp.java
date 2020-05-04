package ru.vyarus.dropwizard.guice.test.jupiter;

import io.dropwizard.Application;
import org.junit.jupiter.api.extension.ExtendWith;
import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.TestDropwizardAppExtension;

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
 *     </ul>
 *     </li>
 *     <li>{@link ru.vyarus.dropwizard.guice.test.jupiter.param.AppPort} annotated int parameter will contain server
 *     port (useful with {@link #randomPorts()})</li>
 *     <li>{@link ru.vyarus.dropwizard.guice.test.jupiter.param.AppAdminPort} annotated int parameter will contain
 *     server admin port (useful with {@link #randomPorts()})</li>
 * </ul>
 * <p>
 * Internally use {@link io.dropwizard.testing.DropwizardTestSupport}.
 * <p>
 * It is possible to apply extension manually using {@link org.junit.jupiter.api.extension.RegisterExtension}
 * and {@link TestDropwizardAppExtension#forApp(Class)} builder. The only difference is declaration type, but in both
 * cases extension will work the same way.
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

    /**
     * Enables random ports usage. Supports both simple and default dropwizard servers. Random ports would be
     * set even if you specify exact configuration file with configured ports (option overrides configuration).
     * <p>
     * To get port numbers in application use lifecycle or test method parameters annotated with
     * {@link ru.vyarus.dropwizard.guice.test.jupiter.param.AppPort} or
     * {@link ru.vyarus.dropwizard.guice.test.jupiter.param.AppAdminPort}. For example:
     * <pre>{@code
     * @BeforeAll
     * static before(@AppPort int port, @AppAdminPort int adminPort) {
     *     baseUrl = "http://localhost:" + port + "/";
     * }
     * }</pre>
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
     * more resides in test). When used with existing configuration file, this parameter will override file definition.
     *
     * @return rest mapping (empty string - do nothing)
     */
    String restMapping() default "";
}
