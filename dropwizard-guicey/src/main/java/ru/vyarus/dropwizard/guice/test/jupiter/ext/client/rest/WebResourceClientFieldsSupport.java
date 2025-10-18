package ru.vyarus.dropwizard.guice.test.jupiter.ext.client.rest;

import org.junit.jupiter.api.extension.ExtensionContext;
import ru.vyarus.dropwizard.guice.test.client.ResourceClient;
import ru.vyarus.dropwizard.guice.test.jupiter.env.TestExtension;
import ru.vyarus.dropwizard.guice.test.jupiter.env.field.AnnotatedField;
import ru.vyarus.dropwizard.guice.test.jupiter.env.field.AnnotatedTestFieldSetup;
import ru.vyarus.dropwizard.guice.test.jupiter.env.listen.EventContext;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.rest.RestStubFieldsSupport;

import java.util.List;

/**
 * {@link ru.vyarus.dropwizard.guice.test.jupiter.ext.client.rest.WebResourceClient} field support implementation.
 * <p>
 * Works with both integration tests and stubs rest.
 *
 * @author Vyacheslav Rusakov
 * @since 17.10.2025
 */
public class WebResourceClientFieldsSupport extends AnnotatedTestFieldSetup<WebResourceClient, ResourceClient> {
    private static final String TEST_REST_CLIENT_FIELDS = "TEST_REST_CLIENT_FIELDS";

    /**
     * Create resource client fields support.
     */
    public WebResourceClientFieldsSupport() {
        super(WebResourceClient.class, ResourceClient.class, TEST_REST_CLIENT_FIELDS);
    }

    @Override
    protected void fieldDetected(final ExtensionContext context,
                                 final AnnotatedField<WebResourceClient, ResourceClient> field) {
        final Class<?> generic = field.getTypeParameters().get(0);
        if (Object.class.equals(generic)) {
            throw new IllegalStateException("Target resource class must be specified"
                    + " as generic (ResourceClient<RestClass>) in field: " + field.toStringField());
        }
    }

    @Override
    protected void registerHooks(final TestExtension extension) {
        // nothing
    }

    @Override
    protected <K> void initializeField(final AnnotatedField<WebResourceClient, ResourceClient> field,
                                       final ResourceClient userValue) {
        // nothing
    }

    @Override
    protected void beforeValueInjection(final EventContext context,
                                        final AnnotatedField<WebResourceClient, ResourceClient> field) {
        // not used
    }

    @Override
    protected ResourceClient injectFieldValue(final EventContext context,
                                              final AnnotatedField<WebResourceClient, ResourceClient> field) {
        final Class<?> resource = field.getTypeParameters().get(0);
        if (context.isWebStarted()) {
            // integration test
            return context.getClient().restClient(resource);
        } else {
            // rest stubs test
            return RestStubFieldsSupport.lookupRestClient(context.getJunitContext())
                    .orElseThrow(() -> new IllegalStateException(String.format(
                            "Resource client can't be used under lightweight guicey test without @StubRest: "
                                    + field.toStringField())))
                    .restClient(resource);
        }
    }

    @Override
    protected void report(final EventContext context,
                          final List<AnnotatedField<WebResourceClient, ResourceClient>> annotatedFields) {
        // not needed
    }

    @Override
    protected void beforeTest(final EventContext context,
                              final AnnotatedField<WebResourceClient, ResourceClient> field,
                              final ResourceClient value) {
        // nothing
    }

    @Override
    protected void afterTest(final EventContext context,
                             final AnnotatedField<WebResourceClient, ResourceClient> field,
                             final ResourceClient value) {
        // reset client defaults
        if (field.getAnnotation().autoReset()) {
            value.reset();
        }
    }
}
