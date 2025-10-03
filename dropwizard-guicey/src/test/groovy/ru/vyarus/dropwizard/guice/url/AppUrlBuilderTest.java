package ru.vyarus.dropwizard.guice.url;

import io.dropwizard.core.Application;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.test.ClientSupport;
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp;
import ru.vyarus.dropwizard.guice.test.jupiter.dw.ClientSupportDwTest;
import ru.vyarus.dropwizard.guice.test.jupiter.param.Jit;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author Vyacheslav Rusakov
 * @since 30.09.2025
 */
public class AppUrlBuilderTest {

    interface ClientCallTest {
        @Test
        default void callClient(ClientSupport client, @Jit AppUrlBuilder builder) {
            Assertions.assertEquals("main", client.target(builder.app("servlet"))
                    .request().buildGet().invoke().readEntity(String.class));

            Assertions.assertEquals("admin", client.target(builder.admin("servlet"))
                    .request().buildGet().invoke().readEntity(String.class));

            Assertions.assertEquals("ok", client.target(builder.rest("sample")).request().buildGet().invoke(String.class));
            Assertions.assertEquals("ok", client.target(builder.rest("sub1/ok")).request().buildGet().invoke(String.class));
            Assertions.assertEquals("ok", client.target(builder.rest("sub2/gg/ok")).request().buildGet().invoke(String.class));

            Assertions.assertEquals("ok", client.target(builder.rest(Resource.class)
                    .method(Resource::sample).build())
                    .request().buildGet().invoke(String.class));
            Assertions.assertEquals("ok", client.target(builder.rest(Resource.class)
                            .method(instance -> instance.sub().sample()).build())
                    .request().buildGet().invoke(String.class));
            Assertions.assertEquals("ok", client.target(builder.rest(Resource.class)
                            .method(instance -> instance.sub("gg").sample()).build())
                    .request().buildGet().invoke(String.class));
        }
    }

    @TestDropwizardApp(App.class)
    @Nested
    class DefaultConfig implements ClientCallTest {

        @Test
        void testClient(@Jit AppUrlBuilder builder) {
            Assertions.assertEquals("http://localhost:8080/", builder.root("/"));
            Assertions.assertEquals("http://localhost:8080/", builder.app("/"));
            Assertions.assertEquals("http://localhost:8081/", builder.admin("/"));
            Assertions.assertEquals("http://localhost:8080/", builder.rest("/"));
            Assertions.assertEquals("http://localhost:8080/sample", builder.rest(Resource.class).method(Resource::sample).build());
            Assertions.assertEquals("http://localhost:8080/sample", builder.rest(Resource.class).method("sample").build());
            Assertions.assertEquals("http://localhost:8080/sample/nm", builder.rest(Resource.class).method("sample", String.class).pathParam("name", "nm").build());
            Assertions.assertEquals("http://localhost:8080/sample/{name}", builder.rest(Resource.class).method("sample", String.class).buildTemplate());
            Assertions.assertEquals("http://localhost:8080/sample/q?q=12", builder.rest(Resource.class).method(res -> res.sample(12)).build());
            Assertions.assertEquals("http://localhost:8080/sample/q?q=1", builder.rest(Resource.class).method("sample", Integer.class).queryParam("q", 1).build());
            Assertions.assertEquals("http://localhost:8080/sample/q?q=1&p=2&p=3", builder.rest(Resource.class).method("sample", Integer.class)
                    .queryParam("q", 1).queryParam("p", 2, 3).build());

            Assertions.assertEquals("http://localhost:8080/sample/q?q=1", builder.rest(Resource.class).path("sample/q?q=%s",1).build());
            Assertions.assertEquals("http://localhost:8080/sample/q?q=1&p=2&p=3", builder.rest(Resource.class).path("sample/q?q=%s&p=%s&p=%s",1, 2, 3).build());

            Assertions.assertEquals("http://localhost:8080/sub1/ok", builder.rest(Resource.class)
                    .subResource("sub1", SubResource.class).method(SubResource::sample).build());
            Assertions.assertEquals("http://localhost:8080/sub1/ok", builder.rest(Resource.class)
                    .method(instance -> instance.sub().sample()).build());
            Assertions.assertEquals("http://localhost:8080/sub2/gg/ok", builder.rest(Resource.class)
                    .method(instance -> instance.sub("gg").sample()).build());

            AppUrlBuilder hosted = builder.forHost("https://some.com");
            Assertions.assertEquals("https://some.com:8080/", hosted.root("/"));
            Assertions.assertEquals("https://some.com:8080/", hosted.app("/"));
            Assertions.assertEquals("https://some.com:8081/", hosted.admin("/"));
            Assertions.assertEquals("https://some.com:8080/", hosted.rest("/"));
            Assertions.assertEquals("https://some.com:8080/sample", hosted.rest(Resource.class).method(Resource::sample).build());

            AppUrlBuilder proxied = builder.forProxy("https://some.com/app");
            Assertions.assertEquals("https://some.com/app/", proxied.root("/"));
            Assertions.assertEquals("https://some.com/app/", proxied.app("/"));
            Assertions.assertEquals("https://some.com/app/", proxied.admin("/"));
            Assertions.assertEquals("https://some.com/app/", proxied.rest("/"));
            Assertions.assertEquals("https://some.com/app/sample", proxied.rest(Resource.class).method(Resource::sample).build());
        }
    }

    @TestDropwizardApp(value = App.class, randomPorts = true)
    @Nested
    class DefaultCustomPortsConfig implements ClientCallTest {

        @Test
        void testClient(@Jit AppUrlBuilder builder) {
            Assertions.assertNotEquals(8080, builder.getAppPort());
            Assertions.assertNotEquals(8081, builder.getAdminPort());

            Assertions.assertEquals("http://localhost:" + builder.getAppPort() + "/", builder.root("/"));
            Assertions.assertEquals("http://localhost:" + builder.getAppPort() + "/", builder.app("/"));
            Assertions.assertEquals("http://localhost:" + builder.getAdminPort() + "/", builder.admin("/"));
            Assertions.assertEquals("http://localhost:" + builder.getAppPort() + "/", builder.rest("/"));
            Assertions.assertEquals("http://localhost:" + builder.getAppPort() + "/sample", builder.rest(Resource.class).method(Resource::sample).build());
            Assertions.assertEquals("http://localhost:" + builder.getAppPort() + "/sample", builder.rest(Resource.class).method("sample").build());
            Assertions.assertEquals("http://localhost:" + builder.getAppPort() + "/sample/nm", builder.rest(Resource.class).method("sample", String.class).pathParam("name", "nm").build());
            Assertions.assertEquals("http://localhost:" + builder.getAppPort() + "/sample/{name}", builder.rest(Resource.class).method("sample", String.class).buildTemplate());
            Assertions.assertEquals("http://localhost:" + builder.getAppPort() +"/sample/q?q=12", builder.rest(Resource.class).method(res -> res.sample(12)).build());
            Assertions.assertEquals("http://localhost:" + builder.getAppPort() + "/sample/q?q=1", builder.rest(Resource.class).method("sample", Integer.class).queryParam("q", 1).build());
            Assertions.assertEquals("http://localhost:" + builder.getAppPort() + "/sample/q?q=1&p=2&p=3", builder.rest(Resource.class).method("sample", Integer.class)
                    .queryParam("q", 1).queryParam("p", 2, 3).build());

            Assertions.assertEquals("http://localhost:" + builder.getAppPort() +"/sample/q?q=1", builder.rest(Resource.class).path("sample/q?q=%s", 1).build());
            Assertions.assertEquals("http://localhost:" + builder.getAppPort() +"/sample/q?q=1&p=2&p=3", builder.rest(Resource.class).path("sample/q?q=%s&p=%s&p=%s",1, 2, 3).build());

            Assertions.assertEquals("http://localhost:" + builder.getAppPort() + "/sub1/ok", builder.rest(Resource.class)
                    .subResource("sub1", SubResource.class).method(SubResource::sample).build());
            Assertions.assertEquals("http://localhost:" + builder.getAppPort() + "/sub1/ok", builder.rest(Resource.class)
                    .method(instance -> instance.sub().sample()).build());
            Assertions.assertEquals("http://localhost:" + builder.getAppPort() + "/sub2/gg/ok", builder.rest(Resource.class)
                    .method(instance -> instance.sub("gg").sample()).build());

            AppUrlBuilder hosted = builder.forHost("https://some.com");
            Assertions.assertEquals("https://some.com:" + builder.getAppPort() + "/", hosted.root("/"));
            Assertions.assertEquals("https://some.com:" + builder.getAppPort() + "/", hosted.app("/"));
            Assertions.assertEquals("https://some.com:" + builder.getAdminPort() + "/", hosted.admin("/"));
            Assertions.assertEquals("https://some.com:" + builder.getAppPort() + "/", hosted.rest("/"));
            Assertions.assertEquals("https://some.com:" + + builder.getAppPort() + "/sample", hosted.rest(Resource.class).method(Resource::sample).build());

            AppUrlBuilder proxied = builder.forProxy("https://some.com/app");
            Assertions.assertEquals("https://some.com/app/", proxied.root("/"));
            Assertions.assertEquals("https://some.com/app/", proxied.app("/"));
            Assertions.assertEquals("https://some.com/app/", proxied.admin("/"));
            Assertions.assertEquals("https://some.com/app/", proxied.rest("/"));
            Assertions.assertEquals("https://some.com/app/sample", proxied.rest(Resource.class).method(Resource::sample).build());
        }
    }


    @TestDropwizardApp(value = App.class, configOverride = {
            "server.applicationContextPath: /app",
            "server.adminContextPath: /admin",
    }, restMapping = "api")
    @Nested
    class ChangedDefaultConfig implements ClientCallTest {

        @Test
        void testClient(@Jit AppUrlBuilder builder) {
            Assertions.assertEquals("http://localhost:8080/", builder.root("/"));
            Assertions.assertEquals("http://localhost:8080/app/", builder.app("/"));
            Assertions.assertEquals("http://localhost:8081/admin/", builder.admin("/"));
            Assertions.assertEquals("http://localhost:8080/app/api/", builder.rest("/"));
            Assertions.assertEquals("http://localhost:8080/app/api/sample", builder.rest(Resource.class).method(Resource::sample).build());
            Assertions.assertEquals("http://localhost:8080/app/api/sample", builder.rest(Resource.class).method("sample").build());
            Assertions.assertEquals("http://localhost:8080/app/api/sample/nm", builder.rest(Resource.class).method("sample", String.class).pathParam("name", "nm").build());
            Assertions.assertEquals("http://localhost:8080/app/api/sample/{name}", builder.rest(Resource.class).method("sample", String.class).buildTemplate());
            Assertions.assertEquals("http://localhost:8080/app/api/sample/q?q=12", builder.rest(Resource.class).method(res -> res.sample(12)).build());
            Assertions.assertEquals("http://localhost:8080/app/api/sample/q?q=1&p=2&p=3", builder.rest(Resource.class).method("sample", Integer.class)
                    .queryParam("q", 1).queryParam("p", 2, 3).build());


            Assertions.assertEquals("http://localhost:8080/app/api/sample/q?q=1", builder.rest(Resource.class).path("sample/q?q=%s", 1).build());
            Assertions.assertEquals("http://localhost:8080/app/api/sample/q?q=1&p=2&p=3", builder.rest(Resource.class).path("sample/q?q=%s&p=%s&p=%s",1, 2, 3).build());

            Assertions.assertEquals("http://localhost:8080/app/api/sub1/ok", builder.rest(Resource.class)
                    .subResource("sub1", SubResource.class).method(SubResource::sample).build());
            Assertions.assertEquals("http://localhost:8080/app/api/sub1/ok", builder.rest(Resource.class)
                    .method(instance -> instance.sub().sample()).build());
            Assertions.assertEquals("http://localhost:8080/app/api/sub2/gg/ok", builder.rest(Resource.class)
                    .method(instance -> instance.sub("gg").sample()).build());

            AppUrlBuilder hosted = builder.forHost("https://some.com");
            Assertions.assertEquals("https://some.com:8080/", hosted.root("/"));
            Assertions.assertEquals("https://some.com:8080/app/", hosted.app("/"));
            Assertions.assertEquals("https://some.com:8081/admin/", hosted.admin("/"));
            Assertions.assertEquals("https://some.com:8080/app/api/", hosted.rest("/"));
            Assertions.assertEquals("https://some.com:8080/app/api/sample", hosted.rest(Resource.class).method(Resource::sample).build());

            AppUrlBuilder proxied = builder.forProxy("https://some.com/v1");
            Assertions.assertEquals("https://some.com/v1/", proxied.root("/"));
            Assertions.assertEquals("https://some.com/v1/app/", proxied.app("/"));
            Assertions.assertEquals("https://some.com/v1/admin/", proxied.admin("/"));
            Assertions.assertEquals("https://some.com/v1/app/api/", proxied.rest("/"));
            Assertions.assertEquals("https://some.com/v1/app/api/sample", proxied.rest(Resource.class).method(Resource::sample).build());
        }
    }

    @TestDropwizardApp(value = App.class, config = "src/test/resources/ru/vyarus/dropwizard/guice/simple-server.yml")
    @Nested
    class SimpleConfig implements ClientCallTest {

        @Test
        void testClient(@Jit AppUrlBuilder builder) {
            Assertions.assertEquals("http://localhost:8080/", builder.root("/"));
            Assertions.assertEquals("http://localhost:8080/", builder.app("/"));
            Assertions.assertEquals("http://localhost:8080/admin/", builder.admin("/"));
            Assertions.assertEquals("http://localhost:8080/rest/", builder.rest("/"));
            Assertions.assertEquals("http://localhost:8080/rest/sample", builder.rest(Resource.class).method(Resource::sample).build());
            Assertions.assertEquals("http://localhost:8080/rest/sample", builder.rest(Resource.class).method("sample").build());
            Assertions.assertEquals("http://localhost:8080/rest/sample/nm", builder.rest(Resource.class).method("sample", String.class).pathParam("name", "nm").build());
            Assertions.assertEquals("http://localhost:8080/rest/sample/{name}", builder.rest(Resource.class).method("sample", String.class).buildTemplate());
            Assertions.assertEquals("http://localhost:8080/rest/sample/q?q=12", builder.rest(Resource.class).method(res -> res.sample(12)).build());
            Assertions.assertEquals("http://localhost:8080/rest/sample/q?q=1", builder.rest(Resource.class).method("sample", Integer.class).queryParam("q", 1).build());
            Assertions.assertEquals("http://localhost:8080/rest/sample/q?q=1&p=2&p=3", builder.rest(Resource.class).method("sample", Integer.class)
                    .queryParam("q", 1).queryParam("p", 2, 3).build());

            Assertions.assertEquals("http://localhost:8080/rest/sample/q?q=1", builder.rest(Resource.class).path("sample/q?q=%s", 1).build());
            Assertions.assertEquals("http://localhost:8080/rest/sample/q?q=1&p=2&p=3", builder.rest(Resource.class).path("sample/q?q=%s&p=%s&p=%s",1, 2, 3).build());

            Assertions.assertEquals("http://localhost:8080/rest/sub1/ok", builder.rest(Resource.class)
                    .subResource("sub1", SubResource.class).method(SubResource::sample).build());
            Assertions.assertEquals("http://localhost:8080/rest/sub1/ok", builder.rest(Resource.class)
                    .method(instance -> instance.sub().sample()).build());
            Assertions.assertEquals("http://localhost:8080/rest/sub2/gg/ok", builder.rest(Resource.class)
                    .method(instance -> instance.sub("gg").sample()).build());

            AppUrlBuilder hosted = builder.forHost("https://some.com");
            Assertions.assertEquals("https://some.com:8080/", hosted.root("/"));
            Assertions.assertEquals("https://some.com:8080/", hosted.app("/"));
            Assertions.assertEquals("https://some.com:8080/admin/", hosted.admin("/"));
            Assertions.assertEquals("https://some.com:8080/rest/", hosted.rest("/"));
            Assertions.assertEquals("https://some.com:8080/rest/sample", hosted.rest(Resource.class).method(Resource::sample).build());

            AppUrlBuilder proxied = builder.forProxy("https://some.com/v1");
            Assertions.assertEquals("https://some.com/v1/", proxied.root("/"));
            Assertions.assertEquals("https://some.com/v1/", proxied.app("/"));
            Assertions.assertEquals("https://some.com/v1/admin/", proxied.admin("/"));
            Assertions.assertEquals("https://some.com/v1/rest/", proxied.rest("/"));
            Assertions.assertEquals("https://some.com/v1/rest/sample", proxied.rest(Resource.class).method(Resource::sample).build());
        }
    }


    public static class App extends Application<Configuration> {

        @Override
        public void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder().build());
        }

        @Override
        public void run(Configuration configuration, Environment environment) throws Exception {
            environment.jersey().register(Resource.class);
            environment.servlets().addServlet("test", new ClientSupportDwTest.Servlet(false))
                    .addMapping("/servlet");
            environment.admin().addServlet("testAdmin", new ClientSupportDwTest.Servlet(true))
                    .addMapping("/servlet");
        }
    }

    @Path("/")
    public static class Resource {

        @GET
        @Path("/sample/")
        public String sample() {
            return "ok";
        }

        @GET
        @Path("/sample/{name}")
        public String sample(@PathParam("name") String name) {
            return "ok";
        }

        @GET
        @Path("/sample/q")
        public String sample(@QueryParam("q") Integer q) {
            return "ok";
        }

        @Path("/sub1/")
        public SubResource sub() {
            return new SubResource();
        }

        @Path("/sub2/{custom}")
        public CustomSubResource sub(@PathParam("custom") String custom) {
            return new CustomSubResource();
        }
    }

    public static class SubResource {
        @GET
        @Path("/ok/")
        public String sample() {
            return "ok";
        }
    }

    // NOTE: sub-resource path is IGNORED
    @Path("/ss")
    public static class CustomSubResource {
        @GET
        @Path("/ok/")
        public String sample() {
            return "ok";
        }
    }

    public static class Servlet extends HttpServlet {

        private boolean admin;

        public Servlet(boolean admin) {
            this.admin = admin;
        }

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            PrintWriter writer = resp.getWriter();
            writer.write(admin ? "admin" : "main");
            writer.flush();
        }
    }
}
