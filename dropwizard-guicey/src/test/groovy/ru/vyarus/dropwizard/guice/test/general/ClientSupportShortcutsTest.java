package ru.vyarus.dropwizard.guice.test.general;

import io.dropwizard.core.Application;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.test.ClientSupport;
import ru.vyarus.dropwizard.guice.test.TestSupport;
import ru.vyarus.dropwizard.guice.test.client.DefaultTestClientFactory;

/**
 * @author Vyacheslav Rusakov
 * @since 26.11.2023
 */
public class ClientSupportShortcutsTest {

    @Test
    void testShortcutMethods() throws Exception {

        TestSupport.runWebApp(App.class, injector -> {
            ClientSupport client = TestSupport.getContextClient();

            // GET
            ResModel res = client.get("sample/get", ResModel.class);
            Assertions.assertThat(res.getFoo()).isEqualTo("get");

            client.get("sample/get2");

            Assertions.assertThatThrownBy(() -> client.get("sample/getErr"))
                    .hasMessageContaining("HTTP 500 Server Error");
            Assertions.assertThatThrownBy(() -> client.get("sample/getErr2", ResModel.class))
                    .hasMessageContaining("HTTP 500 Server Error");


            // POST
            res = client.post("sample/post", null, ResModel.class);
            Assertions.assertThat(res.getFoo()).isEqualTo("post");

            res = client.post("sample/post2", new InModel("tes"), ResModel.class);
            Assertions.assertThat(res.getFoo()).isEqualTo("tes");

            client.post("sample/post3", null);

            Assertions.assertThatThrownBy(() -> client.post("sample/postErr", null))
                    .hasMessageContaining("HTTP 500 Server Error");
            Assertions.assertThatThrownBy(() -> client.post("sample/postErr2", null, ResModel.class))
                    .hasMessageContaining("HTTP 500 Server Error");


            // PUT
            Assertions.assertThatThrownBy(() -> client.put("sample/put", null, ResModel.class))
                    // does not allow null body
                    .hasMessageContaining("Entity must not be null for http method PUT");

            res = client.put("sample/put2", new InModel("tes"), ResModel.class);
            Assertions.assertThat(res.getFoo()).isEqualTo("tes");

            client.put("sample/put3", new InModel("tt"));

            Assertions.assertThatThrownBy(() -> client.put("sample/putErr", new InModel("tt")))
                    .hasMessageContaining("HTTP 500 Server Error");
            Assertions.assertThatThrownBy(() -> client.put("sample/putErr2", new InModel("tt"), ResModel.class))
                    .hasMessageContaining("HTTP 500 Server Error");



            // DELETE
            res = client.delete("sample/del", ResModel.class);
            Assertions.assertThat(res.getFoo()).isEqualTo("delete");

            client.delete("sample/del2");

            Assertions.assertThatThrownBy(() -> client.delete("sample/delErr"))
                    .hasMessageContaining("HTTP 500 Server Error");
            Assertions.assertThatThrownBy(() -> client.delete("sample/delErr2", ResModel.class))
                    .hasMessageContaining("HTTP 500 Server Error");

            return null;
        });
    }

    @Test
    void testClientOutputDisable() throws Exception {

        // run with client output
        String out = TestSupport.captureOutput(() -> {
            TestSupport.runWebApp(App.class, injector -> {
                ClientSupport client = TestSupport.getContextClient();
                client.get("sample/get", Void.class);

                return null;
            });
        });
        Assertions.assertThat(out)
                .contains("[Client action]---------------------------------------------{");

        // run with disabled output
        DefaultTestClientFactory.disableConsoleLog();
        try {
            out = TestSupport.captureOutput(() -> {
                TestSupport.runWebApp(App.class, injector -> {
                    ClientSupport client = TestSupport.getContextClient();
                    client.get("sample/get", Void.class);

                    return null;
                });
            });
            Assertions.assertThat(out)
                    .doesNotContain("[Client action]---------------------------------------------{")
                    // usual logger works instead
                    .contains("1 * Sending client request on thread");
        } finally {
            DefaultTestClientFactory.enableConsoleLog();
        }

        // make sure output enabled again
        out = TestSupport.captureOutput(() -> {
            TestSupport.runWebApp(App.class, injector -> {
                ClientSupport client = TestSupport.getContextClient();
                client.get("sample/get", Void.class);

                return null;
            });
        });
        Assertions.assertThat(out)
                .contains("[Client action]---------------------------------------------{");
    }

    public static class App extends Application<Configuration> {

        @Override
        public void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder().build());
        }

        @Override
        public void run(Configuration configuration, Environment environment) throws Exception {
            environment.jersey().register(SampleRest.class);
        }
    }

    @Produces("application/json")
    @Path("sample/")
    public static class SampleRest {

        @GET
        @Path("/get")
        public ResModel get() {
            return new ResModel("get");
        }

        @GET
        @Path("/get2")
        public void get2() {
        }

        @GET
        @Path("/getErr")
        public void getErrorVoid() {
            throw new IllegalStateException("err");
        }

        @GET
        @Path("/getErr2")
        public ResModel getError() {
            throw new IllegalStateException("err");
        }

        @POST
        @Path("/post")
        public ResModel post() {
            return new ResModel("post");
        }

        @POST
        @Path("/post2")
        public ResModel post2(InModel in) {
            return new ResModel(in.bar);
        }

        @POST
        @Path("/post3")
        public void post3() {
        }

        @POST
        @Path("/postErr")
        public void postErrorVoid() {
            throw new IllegalStateException("err");
        }

        @POST
        @Path("/postErr2")
        public ResModel postError() {
            throw new IllegalStateException("err");
        }

        @PUT
        @Path("/put")
        public ResModel put() {
            return new ResModel("put");
        }

        @PUT
        @Path("/put2")
        public ResModel put2(InModel in) {
            return new ResModel(in.bar);
        }

        @PUT
        @Path("/put3")
        public void put3(InModel in) {
        }

        @PUT
        @Path("/putErr")
        public void putErrorVoid() {
            throw new IllegalStateException("err");
        }

        @PUT
        @Path("/putErr2")
        public ResModel putError() {
            throw new IllegalStateException("err");
        }

        @DELETE
        @Path("/del")
        public ResModel delete() {
            return new ResModel("delete");
        }

        @DELETE
        @Path("/del2")
        public void delete2() {
        }

        @DELETE
        @Path("/delErr")
        public void deleteErrorVoid() {
            throw new IllegalStateException("err");
        }

        @DELETE
        @Path("/delErr2")
        public ResModel deleteError() {
            throw new IllegalStateException("err");
        }
    }

    public static class ResModel {
        private String foo;

        public ResModel() {
        }

        public ResModel(String foo) {
            this.foo = foo;
        }

        public String getFoo() {
            return foo;
        }

        public void setFoo(String foo) {
            this.foo = foo;
        }
    }

    public static class InModel {
        private String bar;

        public InModel() {
        }

        public InModel(String bar) {
            this.bar = bar;
        }

        public String getBar() {
            return bar;
        }

        public void setBar(String bar) {
            this.bar = bar;
        }
    }
}
