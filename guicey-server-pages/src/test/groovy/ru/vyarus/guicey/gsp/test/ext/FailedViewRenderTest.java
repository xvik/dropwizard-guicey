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
import ru.vyarus.guicey.gsp.ServerPagesBundle;
import ru.vyarus.guicey.gsp.views.template.Template;
import ru.vyarus.guicey.gsp.views.template.TemplateView;
import ru.vyarus.guicey.gsp.views.test.ext.ViewModel;
import ru.vyarus.guicey.gsp.views.test.ext.ViewModelTracker;
import ru.vyarus.guicey.gsp.views.test.jupiter.InterceptViewModel;

/**
 * @author Vyacheslav Rusakov
 * @since 10.12.2025
 */
@TestDropwizardApp(value = FailedViewRenderTest.App.class, restMapping = "/rest/", debug = true)
public class FailedViewRenderTest {
    @InterceptViewModel
    ViewModelTracker modelTracker;

    @WebClient(WebClientType.App)
    TestClient<?> client;

    @Test
    void testModel() {

        client.buildGet("sample").expectFailure(500);

        final ViewModel trackedModel = modelTracker.getLastModel();
        Assertions.assertEquals("/sample", trackedModel.getPath());
        Assertions.assertEquals("GET", trackedModel.getHttpMethod());
        Assertions.assertEquals("views/app/sample", trackedModel.getResourcePath());
        Assertions.assertEquals(SampleRest.class, trackedModel.getResourceClass());
        Assertions.assertEquals("get", trackedModel.getResourceMethod().getName());
        Assertions.assertEquals("GET /sample (SampleRest#get)", trackedModel.toString());
        Assertions.assertEquals(200, trackedModel.getStatusCode());

        final Model model = trackedModel.getModel();
        Assertions.assertNotNull(model);
        Assertions.assertEquals("sample", model.getSomething());
    }

    public static class App extends Application<Configuration> {

        @Override
        public void initialize(Bootstrap bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .bundles(
                            ServerPagesBundle.app("UI App", "app", "/")
                                    .mapViews("/views/app/")
                                    .build())
                    .extensions(SampleRest.class)
                    .printStartupTime()
                    .build());
        }

        @Override
        public void run(Configuration configuration, Environment environment) throws Exception {

        }
    }

    public static class Model extends TemplateView {
        private String something;

        public Model(String something) {
            this.something = something;
        }

        public String getSomething() {
            return something;
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
}
