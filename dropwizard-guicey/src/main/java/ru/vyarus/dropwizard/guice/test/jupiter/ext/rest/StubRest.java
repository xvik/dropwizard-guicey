package ru.vyarus.dropwizard.guice.test.jupiter.ext.rest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Resources stubbing: start lightweight rest container (with, probably, only one or a couple of services
 * to test) without web (no servlets, filters, etc. would work). This is the same as dropwizard's
 * {@link io.dropwizard.testing.junit5.ResourceExtension}, but with full guice support. Rest extensions like exception
 * mappers, filters, etc. could also be disabled (including dropwizard default extensions). As guicey knows all
 * registered extensions, it provides them automatically (so, by default, no configuration is required - all jersey
 * resources and extensions are available).
 * <p>
 * It is not quite correct to call it stubs - because this is a fully functional rest (same as in normal application).
 * It is called stub just to highlight customization ability (for example, we can start only resource with just a
 * bunch of enabled extensions).
 * Other stubbing extensions should simplify testing resources (e.g., by mocking authorization support, etc.):
 * {@link ru.vyarus.dropwizard.guice.test.jupiter.ext.stub.StubBean},
 * {@link ru.vyarus.dropwizard.guice.test.jupiter.ext.mock.MockBean},
 * {@link ru.vyarus.dropwizard.guice.test.jupiter.ext.spy.SpyBean},
 * {@link ru.vyarus.dropwizard.guice.test.jupiter.ext.track.TrackBean}.
 * <p>
 * Could be used ONLY with {@link ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp} (or
 * {@link ru.vyarus.dropwizard.guice.test.jupiter.ext.TestGuiceyAppExtension}). Stubbing is enabled by a field
 * declaration: {@code @StubRest RestClient rest}. This declaration activates custom rest container, started
 * on random port and with all rest resources and extensions. As test container does not support web resources (will
 * simply not work), all registered web extensions are disabled (to avoid confusion by console output), together with
 * {@link com.google.inject.servlet.GuiceFilter}.
 * <p>
 * Only one rest stub field could be declared in test! Rest client is injected into the declared field: use it to call
 * rest methods: {@code Something result = rest.get("/relative/rest/path", Something.class)} (see
 * {@link ru.vyarus.dropwizard.guice.test.jupiter.ext.rest.RestClient} class for usage info).
 * <p>
 * To limit started rest resources, simply specify what resources to start (test could start only one resource to
 * test it): {@code @StubRest(Resources1.class, Resource2.class)}. Alternatively, if many resources required,
 * you can disable some resources: {@code @StubRest(disableResources = {Resources1.class, Resource2.class})}.
 * <p>
 * By default, all jersey extensions, declared in application are applied. You can disable all of them:
 * {@code @StubRest(disableAllJerseyExtensions = true)} (note that dropwizard extensions remain!).
 * Or you can specify just required extensions: {@code @StubRest(jerseyExtensions = {Ext1.class, Ext2.class})}.
 * Also, only some extensions could be disabled: {@code @StubRest(disableJerseyExtensions = {Ext1.class, Ext2.class})}.
 * <p>
 * Default dropwizard's exception mappers could be disabled with:
 * {@code @StubRest(disableDropwizardExceptionMappers = true)}. This is very useful for testing rest errors (to
 * receive exception instead of generic 500 response).
 * <p>
 * By default, in-memory container (lightweight, but not all features supported) would be used and grizzly container,
 * if available in classpath. Use {@link #container()} option to force the exact container type (prevent incorrect
 * usage).
 * <p>
 * Use {@code @TestGuiceyApp(debug = true)} to see a list of active rest resources and jersey extensions.
 * The full list of enabled jersey extensions (including dropwizard and jersey core) could be seen with
 * {@code .printJerseyConfig()} option, activated in application (guice builder) or using a hook.
 * <p>
 * Log requests option ({@code @StubRest(logRequests = true)} activates complete requests and responses logging.
 * <p>
 * Warn: the default guicey client ({@link ru.vyarus.dropwizard.guice.test.ClientSupport}) would not work - but you
 * don't need it as a complete rest client provided.
 *
 * @author Vyacheslav Rusakov
 * @since 20.02.2025
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface StubRest {

    /**
     * By default, all resources would be available. Use this option to run a subset of resources.
     *
     * @return resources to use in stub
     * @see #disableResources() to disable some default resources
     */
    Class<?>[] value() default {};

    /**
     * NOTE: if resources specified in {@link #value()} then the disable option would be ignored (all required
     * resources already specified). This option is useful to exclude only some resources from the registered
     * application resources
     * <p>
     * Important: affects only resources, recognized as guicey extensions. Manually registered resources
     * would remain!
     *
     * @return resources to disable
     */
    Class<?>[] disableResources() default {};

    /**
     * By default, all jersey extension, registered in application, would be registered. Use this option to specify
     * exact required extensions (all other application extensions would be disabled).
     * <p>
     * Important: this affects only guicey extensions (all other guicey extension would be simply disabled).
     * To disable core dropwizard exception mappers use {@link #disableDropwizardExceptionMappers()}.
     *
     * @return jersey extensions to use in stub
     */
    Class<?>[] jerseyExtensions() default {};

    /**
     * NOTE: if extensions specified in {@link #jerseyExtensions()} then the disable option would be ignored (all
     * required extensions already specified).
     * <p>
     * Does not affect dropwizard default extensions (only affects extension, controlled by guicey).
     * Dropwizard exception mappers could be disabled with {@link #disableDropwizardExceptionMappers()}.
     *
     * @return true to disable all application jersey extensions
     */
    boolean disableAllJerseyExtensions() default false;

    /**
     * By default, all dropwizard exception mappers registered (same as in real application). For tests, it might be
     * more convenient to disable them and receive direct exception objects after test.
     *
     * @return true dropwizard exception mappers
     */
    boolean disableDropwizardExceptionMappers() default false;

    /**
     * NOTE: if extensions specified in {@link #jerseyExtensions()} then the disable option would be ignored (all
     * required extensions already specified). This option is useful to exclude only some extensions from the registered
     * application jersey extensions.
     * <p>
     * Does not affect dropwizard default extensions (only affects extension, controlled by guicey).
     * Dropwizard exception mappers could be disabled with {@link #disableDropwizardExceptionMappers()}.
     *
     * @return jersey extensions to disable
     */
    Class<?>[] disableJerseyExtensions() default {};

    /**
     * By default, the rest client state is re-set after each test. Client could be reset with manual
     * {@link RestClient#reset()} call.
     *
     * @return false to disable automatic rest client state reset
     */
    boolean autoReset() default true;

    /**
     * Requests log disabled by default (in contrast to {@link ru.vyarus.dropwizard.guice.test.ClientSupport}).
     *
     * @return true to print all requests and responses into console
     */
    boolean logRequests() default false;

    /**
     * By default, use a lightweight in-memory container, but switch to grizzly when it's available in classpath
     * (this is the default behavior of {@link org.glassfish.jersey.test.JerseyTest}).
     *
     * @return required test container policy
     */
    TestContainerPolicy container() default TestContainerPolicy.DEFAULT;
}
