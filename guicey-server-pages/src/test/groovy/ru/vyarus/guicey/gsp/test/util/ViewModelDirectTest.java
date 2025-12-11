package ru.vyarus.guicey.gsp.test.util;

import com.google.inject.Inject;
import io.dropwizard.core.Application;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;
import ru.vyarus.guicey.gsp.ServerPagesBundle;
import ru.vyarus.guicey.gsp.views.template.Template;
import ru.vyarus.guicey.gsp.views.template.TemplateView;
import ru.vyarus.guicey.gsp.views.test.util.TestTemplateContext;

/**
 * @author Vyacheslav Rusakov
 * @since 04.12.2025
 */
@TestGuiceyApp(ViewModelDirectTest.App.class)
public class ViewModelDirectTest {

    @Inject
    SampleRest rest;

    @BeforeEach
    void setUp() {
        // template context is required for views processing, mocking
        new TestTemplateContext().enable();
    }

    @Test
    void testModel() {

        final Model model = rest.get();

        Assertions.assertNotNull(model);
        Assertions.assertEquals("sample", model.getName());
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
    @Template
    public static class SampleRest {

        @GET
        @Path("sample")
        public Model get() {
            return new Model("sample");
        }
    }
}
