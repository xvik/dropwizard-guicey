package ru.vyarus.dropwizard.guice.cases.unproxiablerestmodel;

import com.google.common.base.Preconditions;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.support.DefaultTestApp;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.rest.StubRest;
import ru.vyarus.dropwizard.guice.test.rest.RestClient;

/**
 * @author Vyacheslav Rusakov
 * @since 14.12.2025
 */
@TestGuiceyApp(UnproxiableRestModelTest.App.class)
public class UnsupportedConstructorTest {

    @StubRest
    RestClient restClient;

    @Test
    void testUnproxiedBeanCall() {

        restClient.restClient(SampleRest.class).method(SampleRest::get).invoke();
    }

    public static class App extends DefaultTestApp {
        @Override
        protected GuiceBundle configure() {
            return GuiceBundle.builder()
                    .extensions(SampleRest.class)
                    .build();
        }
    }

    public static class Model {
        private final String name;

        public Model(String name) {
            this.name = Preconditions.checkNotNull(name, "Null name not supported");
        }

        public String getName() {
            return name;
        }
    }

    @Path("/")
    public static class SampleRest {

        @GET
        @Path("/get")
        public Model get() {
            return new Model("sample");
        }
    }
}
