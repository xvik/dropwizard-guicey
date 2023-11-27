package ru.vyarus.dropwizard.guice.test.client;

import io.dropwizard.jersey.jackson.JacksonFeature;
import io.dropwizard.testing.DropwizardTestSupport;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.logging.LoggingFeature;
import ru.vyarus.dropwizard.guice.test.ClientSupport;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Default client factory for {@link ru.vyarus.dropwizard.guice.test.ClientSupport}. Enables INFO logging of
 * all requests and responses into {@link ru.vyarus.dropwizard.guice.test.ClientSupport} logger.
 * Auto register multipart feature if it's available in classpath (through dropwizard-froms).
 * <p>
 * By default, log all requests and responses into system out (console). This could be disabled with
 * {@link #disableConsoleLog()} method (system property).
 *
 * @author Vyacheslav Rusakov
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
                .register(LoggingFeature.builder()
                        .withLogger(System.getProperty(USE_LOGGER) != null
                                // use console log by default
                                ? Logger.getLogger(ClientSupport.class.getName()) : new ConsoleLogger())
                        .verbosity(LoggingFeature.Verbosity.PAYLOAD_TEXT)
                        .level(Level.INFO)
                        .build())
                .property(ClientProperties.CONNECT_TIMEOUT, 1000)
                .property(ClientProperties.READ_TIMEOUT, 5000)
                .property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true);
        try {
            // when dropwizard-forms used automatically register multipart feature
            final Class<?> cls = Class.forName("org.glassfish.jersey.media.multipart.MultiPartFeature");
            builder.register(cls);
        } catch (Exception ignored) {
            // do nothing - no multipart feature available
        }
        return builder.build();
    }

    /**
     * "Hacked" logger to print everything directly into system out. This is required because in tests (almost
     * certainly) logging would not be properly configured and so messages would be "invisible".
     */
    @SuppressWarnings("PMD.SystemPrintln")
    public static class ConsoleLogger extends Logger {
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
