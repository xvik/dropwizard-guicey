package ru.vyarus.dropwizard.guice.test.client;

import io.dropwizard.jersey.jackson.JacksonFeature;
import io.dropwizard.testing.DropwizardTestSupport;
import javax.ws.rs.core.Feature;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.logging.LoggingFeature;
import ru.vyarus.dropwizard.guice.test.ClientSupport;
import ru.vyarus.dropwizard.guice.test.client.util.MultipartCheck;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Default client factory for {@link ru.vyarus.dropwizard.guice.test.ClientSupport}. Enables INFO logging of
 * all requests and responses into {@link ru.vyarus.dropwizard.guice.test.ClientSupport} logger.
 * Auto register multipart feature if it's available in classpath (through dropwizard-froms).
 * <p>
 * By default, log all requests and responses into system out (console). This could be disabled with
 * {@link #disableConsoleLog()} method (system property).
 * <p>
 * NOTE: default {@link org.glassfish.jersey.client.HttpUrlConnectorProvider} does not support PATCH method on
 * jdk &gt; 16 (requires additional --add-opens). To workaround it, use apache or connection provider.
 * <p>
 * If client customization is required, extend this class and override
 * {@link #configure(org.glassfish.jersey.client.JerseyClientBuilder,
 * io.dropwizard.testing.DropwizardTestSupport)} method.
 *
 * @author Vyacheslav Rusakov
 * @see ru.vyarus.dropwizard.guice.test.client.ApacheTestClientFactory
 * @since 15.11.2023
 */
public class DefaultTestClientFactory implements TestClientFactory {

    /**
     * System property name used to disable direct console logs.
     */
    public static final String USE_LOGGER = "USE_LOGGER_FOR_CLIENT";

    /**
     * Disable client logs into system out. Instead, logs would go into
     * {@link ru.vyarus.dropwizard.guice.test.ClientSupport} logger.
     */
    public static void disableConsoleLog() {
        System.setProperty(USE_LOGGER, "true");
    }

    /**
     * Enable client logs into system out. Could be used to revert {@link #disableConsoleLog()} action.
     */
    public static void enableConsoleLog() {
        System.clearProperty(USE_LOGGER);
    }

    @Override
    public JerseyClient create(final DropwizardTestSupport<?> support) {
        final JerseyClientBuilder builder = new JerseyClientBuilder()
                .register(new JacksonFeature(support.getEnvironment().getObjectMapper()))
                // log everything to simplify debug
                .register(createLogger())
                .property(ClientProperties.CONNECT_TIMEOUT, 1000)
                .property(ClientProperties.READ_TIMEOUT, 5000)
                .property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true);
        // apply multipart support, if multipart jar is present in the classpath.
        MultipartCheck.getMultipartFeatureClass().ifPresent(builder::register);
        configure(builder, support);
        return builder.build();
    }

    /**
     * Configure logging feature.
     *
     * @return logging feature
     */
    protected Feature createLogger() {
        return LoggingFeature.builder()
                .withLogger(System.getProperty(USE_LOGGER) != null
                        // use console log by default
                        ? Logger.getLogger(ClientSupport.class.getName()) : new ConsoleLogger())
                .verbosity(LoggingFeature.Verbosity.PAYLOAD_ANY)
                .level(Level.INFO)
                .build();
    }

    /**
     * Provides the ability to customize default client in extending class.
     *
     * @param builder client builder (pre-configured)
     * @param support dropwizard support instance (for accessing environment and configuration)
     */
    protected void configure(final JerseyClientBuilder builder, final DropwizardTestSupport<?> support) {
        // empty
    }

    /**
     * "Hacked" logger to print everything directly into system out. This is required because in tests (almost
     * certainly) logging would not be properly configured and so messages would be "invisible".
     */
    @SuppressWarnings("PMD.SystemPrintln")
    public static class ConsoleLogger extends Logger {

        /**
         * Create a console logger.
         */
        public ConsoleLogger() {
            super(ClientSupport.class.getName(), null);
        }

        @Override
        public boolean isLoggable(final Level level) {
            return true;
        }

        @Override
        public void log(final Level level, final String msg) {
            System.out.println("\n[Client action]---------------------------------------------{");
            System.out.println(msg);
            System.out.println("}----------------------------------------------------------\n");
        }
    }
}
