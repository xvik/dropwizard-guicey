package ru.vyarus.dropwizard.guice;

import io.dropwizard.core.Application;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;

/**
 * @author Vyacheslav Rusakov
 * @since 18.10.2025
 */
@TestGuiceyApp(ApplicationInjectionsTest.App.class)
public class ApplicationInjectionsTest {

    @Inject Service service;

    @Test
    void testApplicationInjection() {
        Assertions.assertThat(service.called).isTrue();
    }

   public static class App extends Application<Configuration> {

        @Inject Service service;

        @Override
        public void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder().build());
        }

        @Override
        public void run(Configuration configuration, Environment environment) throws Exception {
            service.doSomething();
        }
    }

    @Singleton
    public static class Service {
        boolean called;

        public void doSomething() {
            called = true;
        }
    }
}
