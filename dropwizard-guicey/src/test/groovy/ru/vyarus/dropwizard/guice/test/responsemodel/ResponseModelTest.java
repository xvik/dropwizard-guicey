package ru.vyarus.dropwizard.guice.test.responsemodel;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.support.DefaultTestApp;
import ru.vyarus.dropwizard.guice.test.TestSupport;
import ru.vyarus.dropwizard.guice.test.jupiter.setup.responsemodel.ResponseModelInterceptionTest;

/**
 * @author Vyacheslav Rusakov
 * @since 13.12.2025
 */
public class ResponseModelTest {

    @Test
    void testInterceptor() throws Exception {
        ModelInterceptorHook hook = new ModelInterceptorHook(false);
        TestSupport.build(App.class)
                .hooks(hook)
                .runWeb(injector -> {
                    TestSupport.getContextClient().restClient().get("/get", String.class);
                    Assertions.assertEquals(11, hook.getTracker().getLastModel().<Integer>getModel());
                    return null;
                });
    }

    public static class App extends DefaultTestApp {
        @Override
        protected GuiceBundle configure() {
            return GuiceBundle.builder()
                    .extensions(SampleRest.class)
                    .build();
        }
    }

    @Path("/")
    public static class SampleRest {

        @GET
        @Path("get")
        public Integer get() {
            return 11;
        }
    }
}
