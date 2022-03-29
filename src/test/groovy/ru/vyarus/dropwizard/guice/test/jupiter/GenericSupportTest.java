package ru.vyarus.dropwizard.guice.test.jupiter;

import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.testing.DropwizardTestSupport;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.test.ClientSupport;
import ru.vyarus.dropwizard.guice.test.TestSupport;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 * @author Vyacheslav Rusakov
 * @since 30.03.2022
 */
public class GenericSupportTest {

    static DropwizardTestSupport support;

    @Inject
    Service service;

    @BeforeAll
    public static void setup() throws Exception {
        support = TestSupport.webApp(App.class, null);
        // start app
        support.before();
    }

    @BeforeEach
    public void before() {
        // inject services in test
        TestSupport.injectBeans(support, this);
    }

    @AfterAll
    public static void cleanup() {
        if (support != null) {
            support.after();
        }
    }

    @Test
    public void test() {
        Assertions.assertEquals(10, service.doSomething());
        // same bean
        Assertions.assertEquals(service, TestSupport.getBean(support, Service.class));

        // no need to close - it will close with context
        ClientSupport client = TestSupport.webClient(support);
        String res = client.targetRest("/foo").request().buildGet().invoke().readEntity(String.class);
        Assertions.assertEquals("ok", res);
    }

    public static class App extends Application<Configuration> {

        @Override
        public void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .extensions(FooResource.class)
                    .build());
        }

        @Override
        public void run(Configuration configuration, Environment environment) throws Exception {
        }
    }

    @Singleton
    public static class Service {

        int doSomething() {
            return 10;
        }
    }

    @Path("/")
    public static class FooResource {

        @GET
        @Path("/foo")
        public String foo() {
            return "ok";
        }
    }
}
