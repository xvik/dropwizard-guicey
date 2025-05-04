package ru.vyarus.dropwizard.guice.test.rest;

import io.dropwizard.core.setup.Environment;
import io.dropwizard.core.setup.ExceptionMapperBinder;
import io.dropwizard.jersey.jackson.JacksonFeature;
import io.dropwizard.jersey.validation.HibernateValidationBinder;
import jakarta.servlet.DispatcherType;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.GuiceyOptions;
import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook;
import ru.vyarus.dropwizard.guice.module.context.Disables;
import ru.vyarus.dropwizard.guice.module.context.info.ItemInfo;
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.ResourceInstaller;
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycle;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.run.ApplicationRunEvent;
import ru.vyarus.dropwizard.guice.test.rest.support.GuiceyJerseyTest;

import java.util.Collections;
import java.util.EnumSet;
import java.util.function.Predicate;

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
 * <p>
 * Could be used ONLY with lightweight guicey test
 * ({@link ru.vyarus.dropwizard.guice.test.TestSupport#runCoreApp(Class, String, String...)}.
 * Activates custom rest container, started on random port and with all rest resources and extensions. As test
 * container does not support web resources (will simply not work), all registered web extensions are disabled
 * (to avoid confusion by console output), together with {@link com.google.inject.servlet.GuiceFilter}.
 * <p>
 * Rest client should be used to call rest: {@link #getRestClient()}. Use it to call rest methods:
 * {@code Something result = rest.get("/relative/rest/path", Something.class)} (see
 * {@link ru.vyarus.dropwizard.guice.test.rest.RestClient} class for usage info).
 * <p>
 * To limit started rest resources, simply specify what resources to start (test could start only one resource to
 * test it): {@code RestStubsRunner.builder().resources(Resources1.class, Resource2.class)}. Alternatively,
 * if many resources required, you can disable some resources:
 * {@code RestStubsRunner.builder().disableResources(Resources1.class, Resource2.class)}.
 * <p>
 * By default, all jersey extensions, declared in application are applied. You can disable all of them:
 * {@code @RestStubsRunner.builder().disableAllJerseyExtensions(true)} (note that dropwizard extensions remain!).
 * Or you can specify just required extensions:
 * {@code RestStubsRunner.builder().jerseyExtensions(Ext1.class, Ext2.class)}.
 * Also, only some extensions could be disabled:
 * {@code RestStubsRunner.builder().disableJerseyExtensions(Ext1.class, Ext2.class)}.
 * <p>
 * Default dropwizard's exception mappers could be disabled with:
 * {@code RestStubsRunner.builder().disableDropwizardExceptionMappers(true)}. This is very useful for testing rest
 * errors (to receive exception instead of generic 500 response).
 * <p>
 * By default, in-memory container (lightweight, but not all features supported) would be used and grizzly container,
 * if available in classpath. Use {@code RestStubsRunner.builder().container(..)} option to force the exact container
 * type (prevent incorrect usage).
 * <p>
 * The full list of enabled jersey extensions (including dropwizard and jersey core) could be seen with
 * {@code .printJerseyConfig()} option, activated in application (guice builder) or using a hook.
 * <p>
 * Log requests option ({@code RestStubsRunner.builder().logRequests(true)} activates complete requests and responses
 * logging.
 * <p>
 * Warn: the default guicey client ({@link ru.vyarus.dropwizard.guice.test.ClientSupport}) would not work - but you
 * don't need it as a complete rest client provided.
 *
 * @author Vyacheslav Rusakov
 * @since 20.04.2025
 */
public class RestStubsHook implements GuiceyConfigurationHook {

    private final StubRestConfig config;
    private GuiceyJerseyTest jerseyStub;
    private RestClient restClient;

    public RestStubsHook(final StubRestConfig config) {
        this.config = config;
    }

    /**
     * @return builder to configure rest
     */
    public static Builder builder() {
        return new Builder();
    }

    @Override
    public void configure(final GuiceBundle.Builder builder) throws Exception {
        builder
                // disable guice filter (it wouldn't work anyway)
                .option(GuiceyOptions.GuiceFilterRegistration, EnumSet.noneOf(DispatcherType.class))
                // disable all web extensions not working with test rest (they are just ignored - disabling only
                // to indicate)
                .disable(Disables.webExtension().and(Disables.jerseyExtension().negate()))

                // started with listeners to run before application startup event, which is widely used for
                // reporting
                .listen(event -> {
                    if (event.getType().equals(GuiceyLifecycle.ApplicationRun)) {
                        final ApplicationRunEvent evt = (ApplicationRunEvent) event;

                        // manual registration required to reproduce production environment
                        registerDropwizardExtensions(evt.getEnvironment(),
                                config.isDisableDropwizardExceptionMappers());

                        start(config, evt.getEnvironment());
                    }
                })
                .onApplicationShutdown(injector -> stop());

        disableResources(config, builder);
        disableJerseyExtensions(config, builder);
    }

    /**
     * @return jersey test instance
     */
    public GuiceyJerseyTest getJerseyStub() {
        return jerseyStub;
    }

    /**
     * @return rest client, configured to call stubbed rest
     */
    public RestClient getRestClient() {
        return restClient;
    }

    // manual registration required to reproduce production environment see
    // io.dropwizard.testing.common.DropwizardTestResourceConfig.DropwizardTestResourceConfig
    private void registerDropwizardExtensions(final Environment environment,
                                              final boolean disableExceptionMappers) {
        // it might be more convenient to verify exceptions directly, instead of 500 responses
        if (!disableExceptionMappers) {
            environment.jersey().register(new ExceptionMapperBinder(false));
        }
        environment.jersey().register(new JacksonFeature(environment.getObjectMapper()));
        environment.jersey().register(new HibernateValidationBinder(environment.getValidator()));
    }

    private void start(final StubRestConfig config, final Environment environment) {

        jerseyStub = GuiceyJerseyTest.create(environment, config.getContainer(), config.isLogRequests());
        try {
            jerseyStub.setUp();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to start test jersey container", e);
        }
        restClient = new RestClient(jerseyStub);
    }

    private void stop() {
        if (jerseyStub != null) {
            try {
                jerseyStub.tearDown();
            } catch (Exception e) {
                throw new IllegalStateException("Failed to shutdown test jersey container", e);
            }
        }
    }

    private void disableResources(final StubRestConfig config, final GuiceBundle.Builder builder) {
        Predicate<? extends ItemInfo> resourcesDisable = null;
        if (!config.getResources().isEmpty()) {
            // disable all except declared types
            resourcesDisable = Disables.installedBy(ResourceInstaller.class)
                    .and(Disables.type(config.getResources().toArray(Class[]::new)).negate());
        } else if (!config.getDisableResources().isEmpty()) {
            // disable declared
            resourcesDisable = Disables.installedBy(ResourceInstaller.class)
                    .and(Disables.type(config.getDisableResources().toArray(Class[]::new)));
        }
        if (resourcesDisable != null) {
            builder.disable(resourcesDisable);
        }
    }

    private void disableJerseyExtensions(final StubRestConfig config, final GuiceBundle.Builder builder) {
        Predicate<? extends ItemInfo> extDisable = null;
        if (!config.getJerseyExtensions().isEmpty()) {
            // disable all except declared types
            extDisable = Disables.jerseyExtension().and(Disables.installedBy(ResourceInstaller.class).negate())
                    .and(Disables.type(config.getJerseyExtensions().toArray(Class[]::new)).negate());

        } else if (config.isDisableAllJerseyExtensions() || !config.getDisableJerseyExtensions().isEmpty()) {
            extDisable = Disables.jerseyExtension().and(Disables.installedBy(ResourceInstaller.class).negate());
            if (!config.isDisableAllJerseyExtensions()) {
                extDisable = extDisable.and(Disables.type(config.getDisableJerseyExtensions().toArray(Class[]::new)));
            }
        }
        if (extDisable != null) {
            builder.disable(extDisable);
        }
    }

    /**
     * Rest stubs configuration builder.
     */
    public static class Builder {
        private final StubRestConfig config = new StubRestConfig();

        public Builder resources(final Class<?>... resources) {
            Collections.addAll(config.getResources(), resources);
            return this;
        }

        public Builder disableResources(final Class<?>... disableResources) {
            Collections.addAll(config.getDisableResources(), disableResources);
            return this;
        }

        public Builder jerseyExtensions(final Class<?>... jerseyExtensions) {
            Collections.addAll(config.getJerseyExtensions(), jerseyExtensions);
            return this;
        }

        public Builder disableAllJerseyExtensions(final boolean disableAllJerseyExtensions) {
            config.setDisableAllJerseyExtensions(disableAllJerseyExtensions);
            return this;
        }

        public Builder disableDropwizardExceptionMappers(final boolean disableDropwizardExceptionMappers) {
            config.setDisableDropwizardExceptionMappers(disableDropwizardExceptionMappers);
            return this;
        }

        public Builder disableJerseyExtensions(final Class<?>... disableJerseyExtensions) {
            Collections.addAll(config.getDisableJerseyExtensions(), disableJerseyExtensions);
            return this;
        }

        public Builder logRequests(final boolean logRequests) {
            config.setLogRequests(logRequests);
            return this;
        }

        public Builder container(final TestContainerPolicy policy) {
            config.setContainer(policy);
            return this;
        }

        public RestStubsHook build() {
            return new RestStubsHook(config);
        }
    }
}
