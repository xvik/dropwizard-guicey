package ru.vyarus.dropwizard.guice.test.jupiter.ext.rest;

import com.google.common.base.Preconditions;
import com.google.inject.Binder;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.core.setup.ExceptionMapperBinder;
import io.dropwizard.jersey.jackson.JacksonFeature;
import io.dropwizard.jersey.validation.HibernateValidationBinder;
import jakarta.servlet.DispatcherType;
import org.junit.jupiter.api.extension.ExtensionContext;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.GuiceyOptions;
import ru.vyarus.dropwizard.guice.debug.util.RenderUtils;
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo;
import ru.vyarus.dropwizard.guice.module.context.Disables;
import ru.vyarus.dropwizard.guice.module.context.info.ItemInfo;
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.ResourceInstaller;
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycle;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.run.ApplicationRunEvent;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;
import ru.vyarus.dropwizard.guice.test.jupiter.env.field.AnnotatedField;
import ru.vyarus.dropwizard.guice.test.jupiter.env.field.AnnotatedTestFieldSetup;
import ru.vyarus.dropwizard.guice.test.jupiter.env.listen.EventContext;

import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;

/**
 * {@link ru.vyarus.dropwizard.guice.test.jupiter.ext.rest.StubRest} field support implementation.
 * <p>
 * Only one annotated field is supported. Ready to use rest client injected as field value.
 * <p>
 * By default, all rest resources and jersey extensions registered in application are started. Dropwizard default
 * extensions are also registered - so "stub" completely reproduce application state.
 * <p>
 * As rest stub ignores web extensions (servlets, filters) then guicey disables all web extensions to avoid confusion
 * (by logged installed extensions).
 * <p>
 * Jersey container is started just after guicey bundle processing (and not in junit beforeAll) to support
 * guicey jersey report, which is reported just after application initialization (before beforeAll call).
 * <p>
 * AnnotatedTestFieldSetup used because of implemented Nested classes workflow (so nested class could see
 * a rest client declared in root).
 *
 * @author Vyacheslav Rusakov
 * @since 20.02.2025
 */
public class RestStubSupport extends AnnotatedTestFieldSetup<StubRest, RestClient> {

    private static final String TEST_RESOURCES_FIELD = "TEST_RESOURCES";
    private GuiceyJerseyTest jerseyStub;
    private RestClient restStub;

    public RestStubSupport() {
        super(StubRest.class, RestClient.class, TEST_RESOURCES_FIELD);
    }

    @Override
    public void configure(final GuiceBundle.Builder builder) {
        if (!fields.isEmpty()) {
            Preconditions.checkState(fields.size() == 1, "Multiple @" + StubRest.class.getSimpleName()
                    + " fields declared. To avoid confusion with the configuration, only one field is supported.");

            final StubRest config = fields.get(0).getAnnotation();

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
                                    config.disableDropwizardExceptionMappers());

                            start(config, evt.getEnvironment());
                        }
                    })
                    .onApplicationShutdown(injector -> stop());

            disableResources(config, builder);
            disableJerseyExtensions(config, builder);

        }
        // super() not called - no need to override bindings
    }

    @Override
    protected void validateDeclaration(final ExtensionContext context,
                                       final AnnotatedField<StubRest, RestClient> field) {
        // not used
    }


    @Override
    protected <K> void bindFieldValue(final Binder binder,
                                      final AnnotatedField<StubRest, RestClient> field,
                                      final RestClient value) {
        // not used
    }

    @Override
    protected <K> void bindField(final Binder binder, final AnnotatedField<StubRest, RestClient> field) {
        // not used
    }

    @Override
    protected void validateBinding(final EventContext context,
                                   final AnnotatedField<StubRest, RestClient> field) {
        // not used
    }

    @Override
    protected RestClient getFieldValue(final EventContext context,
                                       final AnnotatedField<StubRest, RestClient> field) {
        // inject rest client as value
        return Preconditions.checkNotNull(restStub, "Rest stub is required");
    }

    @Override
    @SuppressWarnings({"PMD.SystemPrintln", "MultipleStringLiterals", "PMD.ConsecutiveLiteralAppends"})
    protected void report(final EventContext context,
                          final List<AnnotatedField<StubRest, RestClient>> annotatedFields) {

        final StringBuilder report = new StringBuilder(500);
        report.append("REST stub (@").append(StubRest.class.getSimpleName())
                .append(") started on ").append(setupContextName).append(":\n")

                .append("\n\tJersey test container factory: ")
                .append(jerseyStub.getTestContainerFactory().getClass().getName())
                .append("\n\tDropwizard exception mappers: ")
                .append(annotatedFields.get(0).getAnnotation().disableDropwizardExceptionMappers()
                        ? "DISABLED" : "ENABLED").append('\n');

        final ExtensionsSelector selector = new ExtensionsSelector(context.getBean(GuiceyConfigurationInfo.class));
        final List<Class<?>> resources = selector.getResources();

        report.append("\n\t").append(resources.size()).append(" resources");
        final int disabledResources = selector.getDisabledResourcesCount();
        if (disabledResources > 0) {
            report.append(" (disabled ").append(disabledResources).append(')');
        }
        report.append(":\n");
        resources.forEach(resource -> report.append(
                String.format("\t\t%s%n", RenderUtils.renderClassLine(resource))));


        final List<Class<?>> extensions = selector.getExtensions();
        report.append("\n\t").append(extensions.size()).append(" jersey extensions");
        final int disabledExtensions = selector.getDisabledExtensionsCount();
        if (disabledExtensions > 0) {
            report.append(" (disabled ").append(disabledExtensions).append(')');
        }
        report.append(":\n");

        extensions.forEach(resource -> report.append(
                String.format("\t\t%s%n", RenderUtils.renderClassLine(resource))));
        report.append("\n\tUse .printJerseyConfig() report to see ALL registered jersey extensions "
                + "(including dropwizard)\n");

        System.out.println(report);
    }

    @Override
    protected void beforeTest(final EventContext context,
                              final AnnotatedField<StubRest, RestClient> field, final RestClient value) {
        // not used
    }

    @Override
    protected void afterTest(final EventContext context,
                             final AnnotatedField<StubRest, RestClient> field, final RestClient value) {
        // reset client defaults
        if (field.getAnnotation().autoReset()) {
            value.reset();
        }
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

    private void start(final StubRest config, final Environment environment) {
        if (!fields.isEmpty()) {
            Preconditions.checkState(!new EventContext(setupContext, false).isWebStarted(),
                    "Resources stubbing is useless when application is fully started. Use it with @"
                            + TestGuiceyApp.class.getSimpleName() + " where web services not started in order to "
                            + "start lightweight container with rest services.");

            jerseyStub = GuiceyJerseyTest.create(environment, config.container(), config.logRequests());
            try {
                jerseyStub.setUp();
            } catch (Exception e) {
                throw new IllegalStateException("Failed to start test jersey container", e);
            }
            restStub = new RestClient(jerseyStub);
        }
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

    private void disableResources(final StubRest config, final GuiceBundle.Builder builder) {
        Predicate<? extends ItemInfo> resourcesDisable = null;
        if (config.value().length > 0) {
            // disable all except declared types
            resourcesDisable = Disables.installedBy(ResourceInstaller.class)
                    .and(Disables.type(config.value()).negate());
        } else if (config.disableResources().length > 0) {
            // disable declared
            resourcesDisable = Disables.installedBy(ResourceInstaller.class)
                    .and(Disables.type(config.disableResources()));
        }
        if (resourcesDisable != null) {
            builder.disable(resourcesDisable);
        }
    }

    private void disableJerseyExtensions(final StubRest config, final GuiceBundle.Builder builder) {
        Predicate<? extends ItemInfo> extDisable = null;
        if (config.jerseyExtensions().length > 0) {
            // disable all except declared types
            extDisable = Disables.jerseyExtension().and(Disables.installedBy(ResourceInstaller.class).negate())
                    .and(Disables.type(config.jerseyExtensions()).negate());

        } else if (config.disableAllJerseyExtensions() || config.disableJerseyExtensions().length > 0) {
            extDisable = Disables.jerseyExtension().and(Disables.installedBy(ResourceInstaller.class).negate());
            if (!config.disableAllJerseyExtensions()) {
                extDisable = extDisable.and(Disables.type(config.disableJerseyExtensions()));
            }
        }
        if (extDisable != null) {
            builder.disable(extDisable);
        }
    }

}
