package ru.vyarus.dropwizard.guice.test.jupiter.setup.spy;

import io.dropwizard.lifecycle.Managed;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.support.DefaultTestApp;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.spy.SpyBean;

import java.util.function.Consumer;

/**
 * @author Vyacheslav Rusakov
 * @since 04.05.2025
 */
@TestGuiceyApp(SpyInitializerTest.App.class)
public class SpyInitializerTest {

    @SpyBean(initializers = Initializer.class)
    Service1 spy1;

    @Test
    void testInitializer() {
        Mockito.verify(spy1, Mockito.times(1)).get(11);
    }

    public static class App extends DefaultTestApp {
        @Override
        protected GuiceBundle configure() {
            return GuiceBundle.builder()
                    .extensions(Mng.class)
                    .build();
        }
    }

    public static class Service1 {

        public String get(int id) {
            return "Hello " + id;
        }
    }

    public static class Mng implements Managed {
        @Inject
        Service1 service1;

        @Override
        public void start() throws Exception {
            service1.get(11);
        }
    }

    public static class Initializer implements Consumer<Service1> {
        @Override
        public void accept(Service1 service1) {
            Mockito.doReturn("spied").when(service1).get(11);
        }
    }
}
