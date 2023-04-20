package ru.vyarus.dropwizard.guice.test.spock;

import io.dropwizard.Application;
import org.spockframework.runtime.extension.ExtensionAnnotation;
import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook;
import ru.vyarus.dropwizard.guice.test.ClientSupport;
import ru.vyarus.dropwizard.guice.test.spock.ext.DropwizardAppExtension;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Dropwizard spock extension. Starts dropwizard application before all tests in class and shutdown after them.
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
 * <li>{@link ClientSupport} annotated with {@link InjectClient} field will be injected
 * with client instance. </li>
 * </ul>
 * <p>
 * Internally based on {@link io.dropwizard.testing.DropwizardTestSupport}.
 *
 * @author Vyacheslav Rusakov
 * @since 03.01.2015
 * @see InjectClient
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@ExtensionAnnotation(DropwizardAppExtension.class)
public @interface UseDropwizardApp {

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

    /**
     * Enables random ports usage. Supports both simple and default dropwizard servers. Random ports would be
     * set even if you specify exact configuration file with configured ports (option overrides configuration).
     * <p>
     * To get port numbers in test use {@link ClientSupport} static field:
     * <pre>{@code @InjectClient ClientSupport client
     *
     * static setupSpec() {
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
}
