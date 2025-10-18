package ru.vyarus.dropwizard.guice.test.jupiter.ext.client;

import org.junit.jupiter.api.extension.ExtensionContext;
import ru.vyarus.dropwizard.guice.test.ClientSupport;
import ru.vyarus.dropwizard.guice.test.client.TestClient;
import ru.vyarus.dropwizard.guice.test.jupiter.env.TestExtension;
import ru.vyarus.dropwizard.guice.test.jupiter.env.field.AnnotatedField;
import ru.vyarus.dropwizard.guice.test.jupiter.env.field.AnnotatedTestFieldSetup;
import ru.vyarus.dropwizard.guice.test.jupiter.env.listen.EventContext;

import java.util.List;

/**
 * {@link ru.vyarus.dropwizard.guice.test.jupiter.ext.client.WebClient} field support implementation.
 * <p>
 * Injects either root {@link ru.vyarus.dropwizard.guice.test.ClientSupport} object or one of specific clients.
 *
 * @author Vyacheslav Rusakov
 * @since 17.10.2025
 */
public class WebClientFieldsSupport extends AnnotatedTestFieldSetup<WebClient, TestClient> {

    private static final String TEST_CLIENT_FIELDS = "TEST_CLIENT_FIELDS";

    /**
     * Create client fields support.
     */
    public WebClientFieldsSupport() {
        super(WebClient.class, TestClient.class, TEST_CLIENT_FIELDS);
    }

    @Override
    protected void fieldDetected(final ExtensionContext context,
                                 final AnnotatedField<WebClient, TestClient> field) {
        final WebClientType type = field.getAnnotation().value();
        if (WebClientType.Support.equals(type) && !ClientSupport.class.equals(field.getType())) {
            throw new IllegalStateException("ClientSupport type must be used for the default @WebClient field: "
                    + field.toStringField());
        }
    }

    @Override
    protected void registerHooks(final TestExtension extension) {
        // nothing
    }

    @Override
    protected <K> void initializeField(final AnnotatedField<WebClient, TestClient> field,
                                       final TestClient userValue) {
        // nothing
    }

    @Override
    protected void beforeValueInjection(final EventContext context,
                                        final AnnotatedField<WebClient, TestClient> field) {
        // nothing
    }

    @Override
    protected TestClient injectFieldValue(final EventContext context,
                                          final AnnotatedField<WebClient, TestClient> field) {
        final ClientSupport support = context.getClient();
        return switch (field.getAnnotation().value()) {
            case Support -> support;
            case App -> support.appClient();
            case Admin -> support.adminClient();
            case Rest -> support.restClient();
        };
    }

    @Override
    protected void report(final EventContext context,
                          final List<AnnotatedField<WebClient, TestClient>> annotatedFields) {
        // no reports required for web client fields
    }

    @Override
    protected void beforeTest(final EventContext context,
                              final AnnotatedField<WebClient, TestClient> field,
                              final TestClient value) {
        // not used
    }

    @Override
    protected void afterTest(final EventContext context,
                             final AnnotatedField<WebClient, TestClient> field,
                             final TestClient value) {
        // reset client defaults
        if (field.getAnnotation().autoReset()) {
            value.reset();
        }
    }
}
