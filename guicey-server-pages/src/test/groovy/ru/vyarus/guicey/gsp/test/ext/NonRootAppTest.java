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
@TestDropwizardApp(value = NonRootAppTest.App.class, restMapping = "/rest/", debug = true)
public class NonRootAppTest {

    @InterceptViewModel
    ViewModelTracker modelTracker;

    @WebClient(WebClientType.App)
    TestClient<?> client;

    @Test
    void testModel() {

        final String html = client.get("/sub/sample", String.class);
        Assertions.assertEquals("name: sample", html);

        final ViewModel trackedModel = modelTracker.getLastModel();
        Assertions.assertEquals("/sub/sample", trackedModel.getPath());
        Assertions.assertEquals("GET", trackedModel.getHttpMethod());
        Assertions.assertEquals("views/app/sample", trackedModel.getResourcePath());
        Assertions.assertEquals(SampleRest.class, trackedModel.getResourceClass());
        Assertions.assertEquals("get", trackedModel.getResourceMethod().getName());
        Assertions.assertEquals("GET /sub/sample (SampleRest#get)", trackedModel.toString());

        final Model model = trackedModel.getModel();
        Assertions.assertNotNull(model);
        Assertions.assertEquals("sample", model.getName());
    }

    public static class App extends Application<Configuration> {

        @Override
        public void initialize(Bootstrap bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .bundles(
                            ServerPagesBundle.app("UI App", "app", "/sub/")
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
}
