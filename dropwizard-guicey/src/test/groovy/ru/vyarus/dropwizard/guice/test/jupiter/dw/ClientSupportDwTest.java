package ru.vyarus.dropwizard.guice.test.jupiter.dw;

import io.dropwizard.core.Application;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp;
import ru.vyarus.dropwizard.guice.test.ClientSupport;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author Vyacheslav Rusakov
 * @since 05.05.2020
 */
public class ClientSupportDwTest {

    interface ClientCallTest {
        @Test
        default void callClient(ClientSupport client) {
            Assertions.assertEquals("main", client.targetMain("servlet")
                    .request().buildGet().invoke().readEntity(String.class));

            Assertions.assertEquals("admin", client.targetAdmin("servlet")
                    .request().buildGet().invoke().readEntity(String.class));

            Assertions.assertEquals("ok", client.targetRest("sample")
                    .request().buildGet().invoke().readEntity(String.class));
        }
    }

    @TestDropwizardApp(App.class)
    @Nested
    class DefaultConfig implements ClientCallTest {

        @Test
        void testClient(ClientSupport client) {
            Assertions.assertEquals("http://localhost:8080/", client.basePathRoot());
            Assertions.assertEquals("http://localhost:8080/", client.basePathMain());
            Assertions.assertEquals("http://localhost:8081/", client.basePathAdmin());
            Assertions.assertEquals("http://localhost:8080/", client.basePathRest());

            Assertions.assertEquals("main", client.target("http://localhost:8080", "servlet")
                    .request().buildGet().invoke().readEntity(String.class));

            Assertions.assertEquals("main", client.getClient().target("http://localhost:8080/servlet")
                    .request().buildGet().invoke().readEntity(String.class));
        }
    }

    @TestDropwizardApp(value = App.class, randomPorts = true)
    @Nested
    class DefaultCustomPortsConfig implements ClientCallTest {

        @Test
        void testClient(ClientSupport client) {
            Assertions.assertNotEquals(8080, client.getPort());
            Assertions.assertNotEquals(8081, client.getAdminPort());

            Assertions.assertEquals("http://localhost:" + client.getPort() + "/", client.basePathRoot());
            Assertions.assertEquals("http://localhost:" + client.getPort() + "/", client.basePathMain());
            Assertions.assertEquals("http://localhost:" + client.getAdminPort() + "/", client.basePathAdmin());
            Assertions.assertEquals("http://localhost:" + client.getPort() + "/", client.basePathRest());
        }
    }


    @TestDropwizardApp(value = App.class, configOverride = {
            "server.applicationContextPath: /app",
            "server.adminContextPath: /admin",
    }, restMapping = "api")
    @Nested
    class ChangedDefaultConfig implements ClientCallTest {

        @Test
        void testClient(ClientSupport client) {
            Assertions.assertEquals("http://localhost:8080/", client.basePathRoot());
            Assertions.assertEquals("http://localhost:8080/app/", client.basePathMain());
            Assertions.assertEquals("http://localhost:8081/admin/", client.basePathAdmin());
            Assertions.assertEquals("http://localhost:8080/app/api/", client.basePathRest());
        }
    }

    @TestDropwizardApp(value = App.class, config = "src/test/resources/ru/vyarus/dropwizard/guice/simple-server.yml")
    @Nested
    class SimpleConfig implements ClientCallTest {

        @Test
        void testClient(ClientSupport client) {
            Assertions.assertEquals("http://localhost:8080/", client.basePathRoot());
            Assertions.assertEquals("http://localhost:8080/", client.basePathMain());
            Assertions.assertEquals("http://localhost:8080/admin/", client.basePathAdmin());
            Assertions.assertEquals("http://localhost:8080/rest/", client.basePathRest());
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
            environment.servlets().addServlet("test", new Servlet(false))
                    .addMapping("/servlet");
            environment.admin().addServlet("testAdmin", new Servlet(true))
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
