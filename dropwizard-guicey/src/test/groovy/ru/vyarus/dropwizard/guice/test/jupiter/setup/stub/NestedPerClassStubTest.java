package ru.vyarus.dropwizard.guice.test.jupiter.setup.stub;

import io.dropwizard.core.Application;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.stub.StubBean;

/**
 * @author Vyacheslav Rusakov
 * @since 09.02.2025
 */
@TestGuiceyApp(NestedPerClassStubTest.App.class)
public class NestedPerClassStubTest {

    @StubBean(Service.class)
    ServiceStub stub;

    @Test
    void testStub() {
        Assertions.assertNotNull(stub);
    }

    @Nested
    class Inner {

        @Inject
        Service service;

        @Test
        void testStubUsed() {
            Assertions.assertEquals(service, stub);
        }
    }

    public static class Service {}

    public static class ServiceStub extends Service {}

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
