package ru.vyarus.dropwizard.guice.test.client.builder.util;

import com.google.common.base.Preconditions;
import jakarta.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds references to responses, wrapped with
 * {@link ru.vyarus.dropwizard.guice.test.client.builder.TestClientResponse} because they might be not consumed
 * and so not closed. This holder aggregates all such requests to close them after a test.
 * <p>
 * The holder itself is stored under the guicey configuration state. On state shutdown (after the test) close method
 * would be called automatically, closing all stale resources.
 * <p>
 * Of course, this mechanism is not a guarantee for all cases when generic test methods used instead of guicey test
 * extensions.
 *
 * @author Vyacheslav Rusakov
 * @since 16.09.2025
 */
public class TestClientResponseCleanup implements AutoCloseable {

    private final List<Response> responses = new ArrayList<>();
    private boolean closed;

    /**
     * Register a response to clean up after the test.
     *
     * @param response response to cleanup
     */
    public void add(final Response response) {
        // should be unreachable because client is closed after test and so it would be impossible to call resource
        // after the application shutdown
        Preconditions.checkState(!closed, "Application already closed");
        responses.add(response);
    }

    @Override
    public void close() throws Exception {
        responses.forEach(Response::close);
        responses.clear();
        closed = true;
    }
}
