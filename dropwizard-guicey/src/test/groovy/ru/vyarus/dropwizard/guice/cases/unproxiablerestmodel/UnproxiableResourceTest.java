package ru.vyarus.dropwizard.guice.cases.unproxiablerestmodel;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.support.DefaultTestApp;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.rest.StubRest;
import ru.vyarus.dropwizard.guice.test.rest.RestClient;

/**
 * @author Vyacheslav Rusakov
 * @since 13.12.2025
 */
@TestGuiceyApp(UnproxiableResourceTest.App.class)
public class UnproxiableResourceTest {

    @StubRest
    RestClient client;

    @Test
    void testResourceProxy() {
        
        Integer res = client.restClient(SampleRest.class).method(SampleRest::get).as(Integer.class);
        Assertions.assertEquals(11, res);
    }

    public static class App extends DefaultTestApp {
        @Override
        protected GuiceBundle configure() {
            return GuiceBundle.builder()
                    .extensions(SampleRest.class)
                    .build();
        }
    }

    public static class Service {
    }

    @Path("/")
    public static class SampleRest {

        private final Service service;

        @Inject
        public SampleRest(Service service) {
            this.service = service;
        }

        @GET
        @Path("/get")
        public Integer get() {
            return 11;
        }
    }
}
