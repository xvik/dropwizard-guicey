package ru.vyarus.dropwizard.guice.test.util.support;

import com.google.common.base.Preconditions;
import io.dropwizard.Configuration;
import io.dropwizard.testing.DropwizardTestSupport;
import javax.annotation.Nullable;
import ru.vyarus.dropwizard.guice.test.ClientSupport;
import ru.vyarus.dropwizard.guice.test.TestSupport;
import ru.vyarus.dropwizard.guice.test.util.client.TestClientFactory;


/**
 * Holds {@link io.dropwizard.testing.DropwizardTestSupport} object during test application execution.
 * Works for junit 5 extensions run and for manual runs with
 * {@link ru.vyarus.dropwizard.guice.test.TestSupport#run(io.dropwizard.testing.DropwizardTestSupport,
 * ru.vyarus.dropwizard.guice.test.TestSupport.RunCallback)} method (or builder run methods).
 *
 * @author Vyacheslav Rusakov
 * @since 15.11.2023
 */
public final class TestSupportHolder {

    private static final ThreadLocal<State> SUPPORT = new ThreadLocal<>();

    private TestSupportHolder() {
    }

    /**
     * Used to register a context support object. Intended to be used ONLY by guicey.
     *
     * @param support       context support object
     * @param clientFactory custom factory object for {@link ru.vyarus.dropwizard.guice.test.ClientSupport}
     *                      (may be null for default factory usage)
     * @throws java.lang.IllegalStateException if any support object already bound in thread
     */
    public static void setContext(final DropwizardTestSupport<?> support,
                                  final @Nullable TestClientFactory clientFactory) {
        setContext(support, TestSupport.webClient(support, clientFactory));
        Preconditions.checkNotNull(support, "Support object can't be null");
        // No check for already bound because junit tests could be hierarchical
        SUPPORT.set(new State(support, TestSupport.webClient(support, clientFactory), true));
    }

    /**
     * Used to register a context support object. Intended to be used ONLY by guicey.
     *
     * @param support context support object
     * @param client  client support instance (from junit 5 extension context; null to create new client)
     * @throws java.lang.IllegalStateException if any support object already bound in thread
     */
    public static void setContext(final DropwizardTestSupport<?> support,
                                  final @Nullable ClientSupport client) {
        Preconditions.checkNotNull(support, "Support object can't be null");
        // No check for already bound because junit tests could be hierarchical
        final boolean manageClient = client == null;
        SUPPORT.set(new State(support, manageClient ? TestSupport.webClient(support) : client, manageClient));
    }

    /**
     * Obtain the test support object, used for test application execution (by junit 5 extension or with
     * {@link ru.vyarus.dropwizard.guice.test.TestSupport#run(io.dropwizard.testing.DropwizardTestSupport,
     * ru.vyarus.dropwizard.guice.test.TestSupport.RunCallback)} (or any derived method, like builder run methods).
     * <p>
     * Use {@link #isContextSet()} to check context initialization
     *
     * @param <C> configuration type
     * @return context support object
     * @throws java.lang.NullPointerException if test support context is not bound in thread
     */
    @SuppressWarnings("unchecked")
    public static <C extends Configuration> DropwizardTestSupport<C> getContext() {
        Preconditions.checkState(isContextSet(), "Test support object not bound in thread");
        return SUPPORT.get().getSupport();
    }

    /**
     * @return true if the support object is bound in thread, false otherwise
     */
    public static boolean isContextSet() {
        return SUPPORT.get() != null;
    }

    /**
     * @return context test web client (in case of junit extensions would be the same client as in extenion)
     */
    public static ClientSupport getClient() {
        Preconditions.checkState(isContextSet(), "Test support object not bound in thread");
        return SUPPORT.get().getClient();
    }

    public static void reset() {
        final State state = SUPPORT.get();
        if (state != null) {
            SUPPORT.remove();
            if (state.isManageClient()) {
                try {
                    state.getClient().close();
                } catch (Exception ignored) {
                    // silent
                }
            }
        }
    }

    /**
     * Thread-bound test support object, user for currently running application.
     */
    private static class State {
        private final DropwizardTestSupport support;
        private final ClientSupport client;
        private final boolean manageClient;

        State(final DropwizardTestSupport support,
              final ClientSupport client,
              final boolean manageClient) {
            this.support = support;
            this.client = client;
            this.manageClient = manageClient;
        }

        /**
         * @return test support object
         */
        public DropwizardTestSupport getSupport() {
            return support;
        }

        /**
         * @return client instance
         */
        public ClientSupport getClient() {
            return client;
        }

        /**
         * @return true if the client managed by holder (must be closed)
         */
        public boolean isManageClient() {
            return manageClient;
        }
    }
}
