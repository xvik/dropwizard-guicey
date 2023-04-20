package ru.vyarus.dropwizard.guice.test.jupiter;

import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.GuiceBundle;

import javax.inject.Inject;

/**
 * @author Vyacheslav Rusakov
 * @since 01.05.2020
 */
public class VoidExtensionsTest {

    @TestDropwizardApp(App.class)
    @Nested
    class DropwizardExtension {

        @Inject
        Environment environment;

        @Test
        void extensionActive() {
            Assertions.assertTrue(environment.getApplicationContext().isStarted());
        }
    }

    @TestGuiceyApp(App.class)
    @Nested
    class GuiceyExtension {

        @Inject
        Environment environment;

        @Test
        void extensionActive() {
            Assertions.assertFalse(environment.getApplicationContext().isStarted());
        }
    }


    public static class App extends Application<Configuration> {

        @Override
        public void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder().build());
        }

        @Override
        public void run(Configuration configuration, Environment environment) throws Exception {

        }
    }
}
