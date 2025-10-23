package ru.vyarus.dropwizard.guice.test.rest;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import org.glassfish.jersey.test.JerseyTest;
import ru.vyarus.dropwizard.guice.test.client.TestClient;
import ru.vyarus.dropwizard.guice.test.rest.support.GuiceyJerseyTest;

import static java.util.Objects.requireNonNull;

/**
 * REST client for test stubbed rest ({@link ru.vyarus.dropwizard.guice.test.jupiter.ext.rest.StubRest}).
 * <p>
 * {@link #client()} provides a raw client, configured with:
 * - Random port
 * - Requests logging ({@code @StubRest(logRequests = true)}, enabled by default)
 * - Enabled restricted headers and method workaround (for url connection, used by in-memory test container)
 * - Set default timeouts to avoid infinite calls
 * - Enabled multipart support (if available in classpath)
 * <p>
 * Provides the same abilities as a client from {@link ru.vyarus.dropwizard.guice.test.ClientSupport}.
 * <p>
 * {@inheritDoc}
 * <p>
 * By default, defaults are reset after each test. So defaults could be specified in the test setup method (to apply
 * the same for all tests in class) or just before method call (in method test directly). Automatic rest could be
 * disabled with {@code @StubRest(autoReset = false)}.
 *
 * @author Vyacheslav Rusakov
 * @since 20.02.2025
 */
public class RestClient extends TestClient<RestClient> {

    private final GuiceyJerseyTest jerseyTest;
    /**
     * Create a client.
     *
     * @param jerseyTest jersey test instance
     */
    public RestClient(final GuiceyJerseyTest jerseyTest) {
        super(null);
        this.jerseyTest = jerseyTest;
    }

    /**
     * Returns the pre-configured {@link javax.ws.rs.client.Client} for this test.
     *
     * @return the {@link JerseyTest} configured {@link javax.ws.rs.client.Client}
     */
    public Client client() {
        return getJerseyTest().client();
    }

    @Override
    protected WebTarget getRoot() {
        return getJerseyTest().target();
    }

    private GuiceyJerseyTest getJerseyTest() {
        return requireNonNull(jerseyTest);
    }
}
