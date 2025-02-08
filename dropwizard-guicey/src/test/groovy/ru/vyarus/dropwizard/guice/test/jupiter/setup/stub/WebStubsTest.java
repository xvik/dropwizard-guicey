package ru.vyarus.dropwizard.guice.test.jupiter.setup.stub;

import io.dropwizard.core.Application;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.test.ClientSupport;
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.stub.StubBean;

/**
 * @author Vyacheslav Rusakov
 * @since 08.02.2025
 */
@TestDropwizardApp(value = WebStubsTest.App.class, debug = true)
public class WebStubsTest {

    @StubBean(Resource.class)
    ResourceStub stub;

    @Test
    void testStubbedResource(ClientSupport client) {
        String res = client.get("/sample", String.class);
        Assertions.assertEquals("override", res);
    }

    public static class App extends Application<Configuration> {

        @Override
        public void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .extensions(Resource.class)
                    .build());
        }

        @Override
        public void run(Configuration configuration, Environment environment) throws Exception {
        }
    }

    @Path("/sample")
    @Produces("application/json")
    public static class Resource {

        @GET
        @Path("/")
        public Response latest() {
            return Response.ok().build();
        }

    }

    public static class ResourceStub extends Resource {

        @Override
        public Response latest() {
            return Response.ok("override").build();
        }

    }

}
