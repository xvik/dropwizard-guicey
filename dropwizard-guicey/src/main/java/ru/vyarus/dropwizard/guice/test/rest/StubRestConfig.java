package ru.vyarus.dropwizard.guice.test.rest;

import java.util.ArrayList;
import java.util.List;

/**
 * Stub rest configuration for {@link ru.vyarus.dropwizard.guice.test.rest.RestStubsRunner}.
 *
 * @author Vyacheslav Rusakov
 * @since 20.04.2025
 */
public class StubRestConfig {

    private final List<Class<?>> resources = new ArrayList<>();
    private final List<Class<?>> disableResources = new ArrayList<>();
    private final List<Class<?>> jerseyExtensions = new ArrayList<>();
    private boolean disableAllJerseyExtensions;
    private boolean disableDropwizardExceptionMappers;
    private final List<Class<?>> disableJerseyExtensions = new ArrayList<>();
    private boolean logRequests;
    private TestContainerPolicy container = TestContainerPolicy.DEFAULT;

    /**
     * By default, all resources would be available. Use this option to run a subset of resources.
     *
     * @return resources to use in stub
     * @see #getDisableResources() to disable some default resources
     */
    public List<Class<?>> getResources() {
        return resources;
    }

    /**
     * NOTE: if resources specified in {@link #getResources()} then the disable option would be ignored (all required
     * resources already specified). This option is useful to exclude only some resources from the registered
     * application resources
     * <p>
     * Important: affects only resources, recognized as guicey extensions. Manually registered resources
     * would remain!
     *
     * @return resources to disable
     */
    public List<Class<?>> getDisableResources() {
        return disableResources;
    }

    /**
     * By default, all jersey extension, registered in application, would be registered. Use this option to specify
     * exact required extensions (all other application extensions would be disabled).
     * <p>
     * Important: this affects only guicey extensions (all other guicey extension would be simply disabled).
     * To disable core dropwizard exception mappers use {@link #setDisableDropwizardExceptionMappers(boolean)}.
     *
     * @return jersey extensions to use in stub
     */
    public List<Class<?>> getJerseyExtensions() {
        return jerseyExtensions;
    }

    /**
     * @return true to disable all application jersey extensions
     */
    public boolean isDisableAllJerseyExtensions() {
        return disableAllJerseyExtensions;
    }

    /**
     * NOTE: if extensions specified in {@link #getJerseyExtensions()} then the disable option would be ignored (all
     * required extensions already specified).
     * <p>
     * Does not affect dropwizard default extensions (only affects extension, controlled by guicey).
     * Dropwizard exception mappers could be disabled with {@link #isDisableDropwizardExceptionMappers()}.
     *
     * @param disableAllJerseyExtensions true to disable all application jersey extensions
     */
    public void setDisableAllJerseyExtensions(final boolean disableAllJerseyExtensions) {
        this.disableAllJerseyExtensions = disableAllJerseyExtensions;
    }

    /**
     * @return true dropwizard exception mappers
     */
    public boolean isDisableDropwizardExceptionMappers() {
        return disableDropwizardExceptionMappers;
    }

    /**
     * By default, all dropwizard exception mappers registered (same as in real application). For tests, it might be
     * more convenient to disable them and receive direct exception objects after test.
     *
     * @param disableDropwizardExceptionMappers true dropwizard exception mappers
     */
    public void setDisableDropwizardExceptionMappers(final boolean disableDropwizardExceptionMappers) {
        this.disableDropwizardExceptionMappers = disableDropwizardExceptionMappers;
    }

    /**
     * NOTE: if extensions specified in {@link #getJerseyExtensions()} then the disable option would be ignored (all
     * required extensions already specified). This option is useful to exclude only some extensions from the registered
     * application jersey extensions.
     * <p>
     * Does not affect dropwizard default extensions (only affects extension, controlled by guicey).
     * Dropwizard exception mappers could be disabled with {@link #isDisableDropwizardExceptionMappers()}.
     *
     * @return jersey extensions to disable
     */
    public List<Class<?>> getDisableJerseyExtensions() {
        return disableJerseyExtensions;
    }

    /**
     * @return true to print all requests and responses into console
     */
    public boolean isLogRequests() {
        return logRequests;
    }

    /**
     * Requests log disabled by default (in contrast to {@link ru.vyarus.dropwizard.guice.test.ClientSupport}).
     *
     * @param logRequests true to print all requests and responses into console
     */
    public void setLogRequests(final boolean logRequests) {
        this.logRequests = logRequests;
    }

    /**
     * @return required test container policy
     */
    public TestContainerPolicy getContainer() {
        return container;
    }

    /**
     * By default, use a lightweight in-memory container, but switch to grizzly when it's available in classpath
     * (this is the default behavior of {@link org.glassfish.jersey.test.JerseyTest}).
     *
     * @param container required test container policy
     */
    public void setContainer(final TestContainerPolicy container) {
        this.container = container;
    }
}
