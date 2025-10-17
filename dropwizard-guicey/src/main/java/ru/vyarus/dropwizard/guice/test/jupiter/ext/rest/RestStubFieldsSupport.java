package ru.vyarus.dropwizard.guice.test.jupiter.ext.rest;

import com.google.common.base.Preconditions;
import org.junit.jupiter.api.extension.ExtensionContext;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.debug.util.RenderUtils;
import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook;
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;
import ru.vyarus.dropwizard.guice.test.jupiter.env.TestExtension;
import ru.vyarus.dropwizard.guice.test.jupiter.env.field.AnnotatedField;
import ru.vyarus.dropwizard.guice.test.jupiter.env.field.AnnotatedTestFieldSetup;
import ru.vyarus.dropwizard.guice.test.jupiter.env.listen.EventContext;
import ru.vyarus.dropwizard.guice.test.rest.RestClient;
import ru.vyarus.dropwizard.guice.test.rest.RestStubsHook;
import ru.vyarus.dropwizard.guice.test.rest.StubRestConfig;
import ru.vyarus.dropwizard.guice.test.rest.support.ExtensionsSelector;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
public class RestStubFieldsSupport extends AnnotatedTestFieldSetup<StubRest, RestClient>
        implements GuiceyConfigurationHook {

    private static final String TEST_RESOURCES_FIELD = "TEST_RESOURCES";
    private static final String STUB_REST_CLIENT_KEY = "STUB_REST_CLIENT_KEY";
    private RestStubsHook restStubs;

    /**
     * Create support.
     */
    public RestStubFieldsSupport() {
        super(StubRest.class, RestClient.class, TEST_RESOURCES_FIELD);
    }

    /**
     * Static lookup for registered stubs rest client. Might be used by other extensions to get access to
     * rest client.
     * <p>
     * Note: stubs rest client is registered AFTER application startup.
     *
     * @param context junit context
     * @return rest client or null if not registered or requested too early
     */
    public static Optional<RestClient> lookupRestClient(final ExtensionContext context) {
        return Optional.ofNullable((RestClient) context
                .getStore(ExtensionContext.Namespace.create(RestStubFieldsSupport.class))
                .get(STUB_REST_CLIENT_KEY));
    }

    @Override
    public void configure(final GuiceBundle.Builder builder) throws Exception {
        builder.onGuiceyStartup((config, env, injector) ->
                Preconditions.checkState(!new EventContext(setupContext, false).isWebStarted(),
                        "Resources stubbing is useless when application is fully started. Use it with @"
                                + TestGuiceyApp.class.getSimpleName() + " where web services not started in "
                                + "order to start lightweight container with rest services."));
    }

    @Override
    protected void registerHooks(final TestExtension extension) {
        Preconditions.checkState(fields.size() == 1, "Multiple @" + StubRest.class.getSimpleName()
                + " fields declared. To avoid confusion with the configuration, only one field is supported.");

        restStubs = new RestStubsHook(getConfig(fields.get(0).getAnnotation()));

        extension.hooks(restStubs, this);
    }

    @Override
    protected void fieldDetected(final ExtensionContext context,
                                 final AnnotatedField<StubRest, RestClient> field) {
        // not used
    }

    @Override
    protected <K> void initializeField(final AnnotatedField<StubRest, RestClient> field, final RestClient userValue) {
        // not used
    }

    @Override
    protected void beforeValueInjection(final EventContext context,
                                        final AnnotatedField<StubRest, RestClient> field) {
        context.getJunitContext().getStore(ExtensionContext.Namespace.create(RestStubFieldsSupport.class))
                .put(STUB_REST_CLIENT_KEY, restStubs.getRestClient());
    }

    @Override
    protected RestClient injectFieldValue(final EventContext context,
                                          final AnnotatedField<StubRest, RestClient> field) {
        // inject rest client as value
        return Preconditions.checkNotNull(restStubs.getRestClient(), "Rest stub is required");
    }

    @Override
    @SuppressWarnings({"PMD.SystemPrintln", "MultipleStringLiterals", "PMD.ConsecutiveLiteralAppends"})
    protected void report(final EventContext context,
                          final List<AnnotatedField<StubRest, RestClient>> annotatedFields) {

        final StringBuilder report = new StringBuilder(500);
        report.append("REST stub (@").append(StubRest.class.getSimpleName())
                .append(") started on ").append(setupContextName).append(":\n")

                .append("\n\tJersey test container factory: ")
                .append(restStubs.getJerseyStub().getTestContainerFactory().getClass().getName())
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

    private StubRestConfig getConfig(final StubRest annotation) {
        final StubRestConfig config = new StubRestConfig();
        Collections.addAll(config.getResources(), annotation.value());
        Collections.addAll(config.getDisableResources(), annotation.disableResources());
        Collections.addAll(config.getJerseyExtensions(), annotation.jerseyExtensions());
        config.setDisableAllJerseyExtensions(annotation.disableAllJerseyExtensions());
        config.setDisableDropwizardExceptionMappers(annotation.disableDropwizardExceptionMappers());
        Collections.addAll(config.getDisableJerseyExtensions(), annotation.disableJerseyExtensions());
        config.setLogRequests(annotation.logRequests());
        config.setContainer(annotation.container());
        return config;
    }

}
