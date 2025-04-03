package ru.vyarus.dropwizard.guice.test.jupiter.setup.stub;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.support.DefaultTestApp;
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

    public static class App extends DefaultTestApp {

        @Override
        protected GuiceBundle configure() {
            return GuiceBundle.builder()
                    .extensions(Resource.class)
                    .build();
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
