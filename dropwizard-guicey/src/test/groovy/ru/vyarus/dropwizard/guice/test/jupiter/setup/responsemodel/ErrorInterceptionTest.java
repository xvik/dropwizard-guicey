package ru.vyarus.dropwizard.guice.test.jupiter.setup.responsemodel;

import io.dropwizard.jersey.errors.ErrorMessage;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.support.DefaultTestApp;
import ru.vyarus.dropwizard.guice.test.client.ResourceClient;
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.client.rest.WebResourceClient;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.responsemodel.InterceptModel;
import ru.vyarus.dropwizard.guice.test.responsemodel.ModelTracker;
import ru.vyarus.dropwizard.guice.test.responsemodel.model.ResponseModel;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Vyacheslav Rusakov
 * @since 12.12.2025
 */
@TestDropwizardApp(ErrorInterceptionTest.App.class)
public class ErrorInterceptionTest {

    @InterceptModel(interceptErrors = true)
    ModelTracker tracker;

    @WebResourceClient
    ResourceClient<SampleResource> rest;

    @Test
    void testErrorInterception() {

        // WHEN calling simple get
        rest.method(SampleResource::get).invoke();

        final ResponseModel model = tracker.getLastModel();
        assertThat(model).isNotNull();
        assertThat(model.<Object>getModel()).isInstanceOf(ErrorMessage.class);
        ErrorMessage error = model.getModel();
        assertThat(error.getMessage()).contains("There was an error processing your request. It has been logged");
    }

    public static class App extends DefaultTestApp {
        @Override
        protected GuiceBundle configure() {
            return GuiceBundle.builder()
                    .extensions(SampleResource.class)
                    .build();
        }
    }

    @Path("/")
    public static class SampleResource {

        @GET
        @Path("getError")
        public Integer get() {
            throw new IllegalStateException("error");
        }
    }
}
