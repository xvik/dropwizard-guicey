package ru.vyarus.guicey.gsp.test.ext;

import io.dropwizard.core.Application;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
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
import ru.vyarus.guicey.gsp.ServerPagesBundle;
import ru.vyarus.guicey.gsp.views.template.Template;
import ru.vyarus.guicey.gsp.views.template.TemplateView;

/**
 * @author Vyacheslav Rusakov
 * @since 10.12.2025
 */
@TestDropwizardApp(value = MultipleViewCallsTest.App.class, restMapping = "/rest/", debug = true)
public class MultipleViewCallsTest {

    @InterceptModel
    ModelTracker modelTracker;

    @WebClient(WebClientType.App)
    TestClient<?> client;

    @Test
    void testModel() {

        // call 1
        String html = client.get("sample", String.class);
        Assertions.assertEquals("name: sample", html);

        ResponseModel trackedModel = modelTracker.getLastModel();
        Assertions.assertEquals("GET", trackedModel.getHttpMethod());
        Assertions.assertEquals("/views/app/sample", trackedModel.getResourcePath());
        Assertions.assertEquals(SampleRest.class, trackedModel.getResourceClass());
        Assertions.assertEquals("get", trackedModel.getResourceMethod().getName());
        Model model = trackedModel.getModel();

        Assertions.assertNotNull(model);
        Assertions.assertEquals("sample", model.getName());

        // call 2
        html = client.get("2/sample", String.class);
        Assertions.assertEquals("name: sample2", html);

        trackedModel = modelTracker.getLastModel();
        Assertions.assertEquals("GET", trackedModel.getHttpMethod());
        Assertions.assertEquals("/views/app/2/sample", trackedModel.getResourcePath());
        Assertions.assertEquals(SampleRest2.class, trackedModel.getResourceClass());
        Assertions.assertEquals("get", trackedModel.getResourceMethod().getName());
        model = trackedModel.getModel();

        Assertions.assertNotNull(model);
        Assertions.assertEquals("sample2", model.getName());

        Assertions.assertEquals(2, modelTracker.getViewModels().size());
        Assertions.assertEquals(1, modelTracker.getViewModels(SampleRest.class).size());
    }

    public static class App extends Application<Configuration> {

        @Override
        public void initialize(Bootstrap bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .bundles(
                            ServerPagesBundle.app("UI App", "app", "/")
                                    .mapViews("/views/app/")
                                    .build())
                    .extensions(SampleRest.class, SampleRest2.class)
                    .build());
        }

        @Override
        public void run(Configuration configuration, Environment environment) throws Exception {

        }
    }

    public static class Model extends TemplateView {
        private String name;

        public Model(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    // no leading slash!
    @Path("/views/app")
    @Produces(MediaType.TEXT_HTML)
    @Template("sample.ftl")
    public static class SampleRest {

        @GET
        @Path("sample")
        public Model get() {
            return new Model("sample");
        }
    }

    @Path("/views/app/2/")
    @Produces(MediaType.TEXT_HTML)
    @Template("sample.ftl")
    public static class SampleRest2 {

        @GET
        @Path("sample")
        public Model get() {
            return new Model("sample2");
        }
    }
}
