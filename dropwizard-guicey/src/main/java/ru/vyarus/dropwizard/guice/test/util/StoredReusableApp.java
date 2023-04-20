package ru.vyarus.dropwizard.guice.test.util;

import io.dropwizard.testing.DropwizardTestSupport;
import org.junit.jupiter.api.extension.ExtensionContext;
import ru.vyarus.dropwizard.guice.test.ClientSupport;

/**
 * Storage value for reusable application. Value would be created by the first test (using reusable app) and
 * would be closed after all tests (automatically by junit).
 * <p>
 * Reusable app is identified in root storage by declaration class name because only one application extension
 * could be used in single test.
 *
 * @author Vyacheslav Rusakov
 * @since 19.12.2022
 */
public class StoredReusableApp implements ExtensionContext.Store.CloseableResource {

    private final Class<?> declaration;
    private final String source;
    private final DropwizardTestSupport<?> support;
    private final ClientSupport client;

    public StoredReusableApp(final Class<?> declaration,
                             final String source,
                             final DropwizardTestSupport<?> support,
                             final ClientSupport client) {
        this.declaration = declaration;
        this.source = source;
        this.support = support;
        this.client = client;
    }

    /**
     * @return base test class where extension was declared
     */
    public Class<?> getDeclaration() {
        return declaration;
    }

    /**
     * @return declaration source (base class + annotation or field name)
     */
    public String getSource() {
        return source;
    }

    /**
     * @return reusable support object
     */
    public DropwizardTestSupport<?> getSupport() {
        return support;
    }

    /**
     * @return reusable client instance
     */
    public ClientSupport getClient() {
        return client;
    }

    @Override
    public void close() throws Exception {
        support.after();
        client.close();
    }

    @Override
    public String toString() {
        return source;
    }
}
