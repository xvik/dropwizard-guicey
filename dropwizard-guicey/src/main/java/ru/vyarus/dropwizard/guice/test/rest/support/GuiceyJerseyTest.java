package ru.vyarus.dropwizard.guice.test.rest.support;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.dropwizard.core.setup.Environment;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.glassfish.jersey.internal.ServiceFinder;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.test.DeploymentContext;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.ServletDeploymentContext;
import org.glassfish.jersey.test.TestProperties;
import org.glassfish.jersey.test.inmemory.InMemoryTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import ru.vyarus.dropwizard.guice.test.client.DefaultTestClientFactory;
import ru.vyarus.dropwizard.guice.test.client.util.MultipartCheck;
import ru.vyarus.dropwizard.guice.test.rest.TestContainerPolicy;

import java.net.URI;
import java.util.logging.Level;
import java.util.stream.StreamSupport;

/**
 * Jersey rest stubs implementation (based on {@link org.glassfish.jersey.test.JerseyTest}).
 * Configures:
 * <ul>
 * <li>Random port
 * <li>Requests logging
 * <li>Enables restricted headers and method workaround (for url connection)
 * <li>Set default timeouts to avoid infinite calls
 * <li>Enable multipart support (if available in classpath)
 * </ul>
 * <p>
 * Application deployment context used (same as in normal dropwizard application). Guicey disables not wanted
 * extensions, if required.
 * <p>
 * Assume 2 possible containers: in-memory (may not support some rest features) and grizzly.
 * By default, should delegate container selection to {@link org.glassfish.jersey.test.JerseyTest}, which
 * selects grizzly, if available or use in-memory. Also, supports custom system property.
 *
 * @author Vyacheslav Rusakov
 * @since 25.02.2025
 */
public class GuiceyJerseyTest extends JerseyTest {

    private static Environment environment;
    private static TestContainerPolicy policy;
    private final boolean logRequests;

    /**
     * Create jersey test.
     * NOTE Environment can't be used in constructor directly due to configureDeployment() override
     *
     * @param logRequests true to log requests and responses
     */
    protected GuiceyJerseyTest(final boolean logRequests) {
        this.logRequests = logRequests;

        // allow restricted headers by default
        // https://eclipse-ee4j.github.io/jersey.github.io/documentation/latest/client.html#d0e5292
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
    }

    /**
     * @param environment environment
     * @param policy      container policy
     * @param logRequests log requests and responses
     * @return jersey test instance
     */
    @SuppressFBWarnings("EI_EXPOSE_STATIC_REP2")
    public static GuiceyJerseyTest create(final Environment environment,
                                          final TestContainerPolicy policy,
                                          final boolean logRequests) {
        synchronized (GuiceyJerseyTest.class) {
            // have to use static variable because environment requested from super constructor!
            GuiceyJerseyTest.environment = environment;
            GuiceyJerseyTest.policy = policy;
            return new GuiceyJerseyTest(logRequests);
        }
    }

    @Override
    @SuppressWarnings("PMD.ExhaustiveSwitchHasDefault")
    public TestContainerFactory getTestContainerFactory() {
        final TestContainerFactory res;
        switch (policy) {
            case DEFAULT:
                res = super.getTestContainerFactory();
                break;
            case IN_MEMORY:
                res = new InMemoryTestContainerFactory();
                break;
            case GRIZZLY:
                // use service loader to load available factories
                res = StreamSupport.stream(ServiceFinder
                                .find(TestContainerFactory.class).spliterator(), false)
                        .filter(factory ->
                                "org.glassfish.jersey.test.grizzly.GrizzlyTestContainerFactory"
                                        .equals(factory.getClass().getName()))
                        .findFirst().orElseThrow(() -> new IllegalStateException(
                                "org.glassfish.jersey.test.grizzly.GrizzlyTestContainerFactory is not available in "
                                        + "classpath. Add `org.glassfish.jersey.test-framework.providers:jersey-test-"
                                        + "framework-provider-grizzly2` dependency (version managed by dropwizard BOM)"
                        ));
                break;
            default:
                throw new IllegalStateException("Unsupported policy: " + policy);
        }
        return res;
    }

    @Override
    protected URI getBaseUri() {
        // can't be in constructor - too late
        forceSet(TestProperties.CONTAINER_PORT, "0");
        return super.getBaseUri();
    }

    @Override
    protected DeploymentContext configureDeployment() {
        // NOTE: called from super constructor and so can't see variables!
        return ServletDeploymentContext
                // use application config (almost the same as with normal startup)
                .builder(environment.jersey().getResourceConfig())
                .build();
    }

    @Override
    protected void configureClient(final ClientConfig clientConfig) {
        // log everything to simplify debug
        if (logRequests) {
            clientConfig.register(LoggingFeature.builder()
                    .withLogger(new DefaultTestClientFactory.ConsoleLogger())
                    .verbosity(LoggingFeature.Verbosity.PAYLOAD_ANY)
                    .level(Level.INFO)
                    .build());
        }
        // prevent infinite loading
        clientConfig.property(ClientProperties.CONNECT_TIMEOUT, 1000);
        clientConfig.property(ClientProperties.READ_TIMEOUT, 5000);
        // https://eclipse-ee4j.github.io/jersey.github.io/documentation/latest/client.html#d0e5292
        clientConfig.property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true);

        // when dropwizard-forms used, automatically register multipart feature
        MultipartCheck.getMultipartFeatureClass().ifPresent(clientConfig::register);
    }
}
