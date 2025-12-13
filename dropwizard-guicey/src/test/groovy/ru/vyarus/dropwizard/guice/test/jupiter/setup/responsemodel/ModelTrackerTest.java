package ru.vyarus.dropwizard.guice.test.jupiter.setup.responsemodel;

import io.dropwizard.core.Application;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.test.client.TestClient;
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.client.WebClient;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.client.WebClientType;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.responsemodel.InterceptModel;
import ru.vyarus.dropwizard.guice.test.responsemodel.ModelTracker;
import ru.vyarus.dropwizard.guice.test.responsemodel.model.ResponseModel;

/**
 * @author Vyacheslav Rusakov
 * @since 12.12.2025
 */
@TestDropwizardApp(ModelTrackerTest.App.class)
public class ModelTrackerTest {

    @InterceptModel
    ModelTracker tracker;

    @WebClient(WebClientType.App)
    TestClient<?> client;

    @Test
    void testModel() {

        // call 1
        client.get("sample", String.class);
        ResponseModel trackedModel = tracker.getLastModel();
        Assertions.assertEquals("GET", trackedModel.getHttpMethod());
        Assertions.assertEquals("/sample", trackedModel.getResourcePath());
        Assertions.assertEquals(SampleRest.class, trackedModel.getResourceClass());
        Assertions.assertEquals("get", trackedModel.getResourceMethod().getName());
        Assertions.assertEquals(11, trackedModel.<Integer>getModel());

        // call 2
        client.get("2/sample", String.class);
        trackedModel = tracker.getLastModel();
        Assertions.assertEquals("GET", trackedModel.getHttpMethod());
        Assertions.assertEquals("/2/sample", trackedModel.getResourcePath());
        Assertions.assertEquals(SampleRest2.class, trackedModel.getResourceClass());
        Assertions.assertEquals("get", trackedModel.getResourceMethod().getName());
        Assertions.assertEquals("sample", trackedModel.getModel());

        Assertions.assertEquals(2, tracker.getViewModels().size());
        Assertions.assertEquals(1, tracker.getViewModels(SampleRest.class).size());
        Assertions.assertEquals(1, tracker.getViewModels(SampleRest2.class).size());
        Assertions.assertEquals(11, tracker.getLastModel(SampleRest.class).<Integer>getModel());
    }

    public static class App extends Application<Configuration> {

        @Override
        public void initialize(Bootstrap bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .extensions(SampleRest.class, SampleRest2.class)
                    .build());
        }

        @Override
        public void run(Configuration configuration, Environment environment) throws Exception {

        }
    }

    // no leading slash!
    @Path("/")
    public static class SampleRest {

        @GET
        @Path("/sample")
        public Integer get() {
            return 11;
        }
    }

    @Path("/2")
    public static class SampleRest2 {

        @GET
        @Path("sample")
        public String get() {
            return "sample";
        }
    }
}
