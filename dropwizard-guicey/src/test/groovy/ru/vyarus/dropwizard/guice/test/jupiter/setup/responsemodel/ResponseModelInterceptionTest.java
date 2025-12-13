package ru.vyarus.dropwizard.guice.test.jupiter.setup.responsemodel;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.support.DefaultTestApp;
import ru.vyarus.dropwizard.guice.test.client.ResourceClient;
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.client.rest.WebResourceClient;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.responsemodel.InterceptModel;
import ru.vyarus.dropwizard.guice.test.responsemodel.ModelTracker;
import ru.vyarus.dropwizard.guice.test.responsemodel.model.ResponseModel;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Vyacheslav Rusakov
 * @since 12.12.2025
 */
@TestDropwizardApp(value = ResponseModelInterceptionTest.App.class, debug = true)
public class ResponseModelInterceptionTest {

    @InterceptModel
    ModelTracker tracker;

    @WebResourceClient
    ResourceClient<TestResource> rest;

    @Test
    void testSimpleModel() {

        // WHEN calling simple get
        rest.method(TestResource::get).asString();

        final ResponseModel model = tracker.getLastModel();
        assertThat((Integer) model.getModel()).isEqualTo(12);
        assertThat(model).extracting(
                        ResponseModel::getHttpMethod,
                        ResponseModel::getResourcePath,
                        ResponseModel::getResourceClass,
                        responseModel -> responseModel.getResourceMethod().getName(),
                        ResponseModel::getStatusCode)
                .containsExactly(
                        "GET",
                        "/test/get",
                        TestResource.class,
                        "get",
                        200
                );
        assertThat(model.toString()).isEqualTo("GET 200 /test/get (TestResource#get)");
    }

    @Test
    void testNullModel() {

        // WHEN calling simple get
        rest.method(TestResource::getNull).asString();

        final ResponseModel model = tracker.getLastModel();
        assertThat(model).isNull();
    }

    @Test
    void testRawResponse() {

        // WHEN calling simple get
        rest.method(TestResource::getResponse).asString();

        final ResponseModel model = tracker.getLastModel();
        assertThat((Integer) model.getModel()).isEqualTo(12);
    }

    @Test
    void testVoidResponse() {

        // WHEN calling simple get
        rest.method(instance -> instance.post(11)).asString();

        final ResponseModel model = tracker.getLastModel();
        assertThat(model).isNull();
    }

    @Test
    void testFileDownload() {

        // WHEN calling simple get
        rest.method(TestResource::download).asString();

        final ResponseModel model = tracker.getLastModel();
        assertThat((File) model.getModel()).isFile();

        File file = model.getModel();
        assertThat(file.getName()).isEqualTo("logback.xml");
    }

    @Test
    void testErrorResponse() {

        // WHEN calling simple get
        rest.method(TestResource::getError).invoke();

        final ResponseModel model = tracker.getLastModel();
        assertThat(model).isNull();
    }

    public static class App extends DefaultTestApp {

        @Override
        protected GuiceBundle configure() {
            return GuiceBundle.builder()
                    .extensions(TestResource.class)
                    .build();
        }
    }

    @Path("/test")
    public static class TestResource {

        @GET
        @Path("/get")
        public Integer get() {
            return 12;
        }

        @GET
        @Path("/getNull")
        public Integer getNull() {
            return null;
        }

        @GET
        @Path("/getRaw")
        public Response getResponse() {
            return Response.ok(12).build();
        }

        @POST
        @Path("/post/{id}")
        public void post(@PathParam("id") Integer id) {
        }

        @GET
        @Path("/download")
        public File download() {
            return new File("src/test/resources/logback.xml");
        }

        @GET
        @Path("/getError")
        public Integer getError() {
            throw new IllegalStateException("error");
        }
    }
}
