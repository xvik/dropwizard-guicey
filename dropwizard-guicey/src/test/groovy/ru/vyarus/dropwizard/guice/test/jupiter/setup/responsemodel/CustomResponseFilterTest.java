package ru.vyarus.dropwizard.guice.test.jupiter.setup.responsemodel;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.support.DefaultTestApp;
import ru.vyarus.dropwizard.guice.test.client.ResourceClient;
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.client.rest.WebResourceClient;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.responsemodel.InterceptModel;
import ru.vyarus.dropwizard.guice.test.responsemodel.ModelTracker;
import ru.vyarus.dropwizard.guice.test.responsemodel.model.ResponseModel;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Vyacheslav Rusakov
 * @since 12.12.2025
 */
@TestDropwizardApp(CustomResponseFilterTest.App.class)
public class CustomResponseFilterTest {

    @InterceptModel
    ModelTracker tracker;

    @WebResourceClient
    ResourceClient<SampleRest> rest;

    @Test
    void testSimpleModel() {

        // WHEN calling simple get
        rest.method(SampleRest::get).asString();

        final ResponseModel model = tracker.getLastModel();
        assertThat((Object) model.getModel()).isInstanceOf(String.class).isEqualTo("hello");
    }

    @Test
    void testCustomizedResponseInterception() {

    }

    public static class App extends DefaultTestApp {
        @Override
        protected GuiceBundle configure() {
            return GuiceBundle.builder()
                    .extensions(CustomResponseFilter.class, SampleRest.class)
                    .build();
        }
    }

    @Provider
    public static class CustomResponseFilter implements ContainerResponseFilter {
        @Override
        public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
            responseContext.setEntity("hello");
        }
    }
    
    @Path("/")
    public static class SampleRest {

        @GET
        @Path("/get")
        public Integer get() {
            return 12;
        }
    }
}
