package ru.vyarus.dropwizard.guice.test.spy;

import io.dropwizard.lifecycle.Managed;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ru.vyarus.dropwizard.guice.support.DefaultTestApp;
import ru.vyarus.dropwizard.guice.test.TestSupport;

/**
 * @author Vyacheslav Rusakov
 * @since 04.05.2025
 */
public class SpyInitializerTest {

    @Test
    void testSpy() throws Exception {
        SpiesHook hook = new SpiesHook();
        final SpyProxy<Service1> proxy = hook.spy(Service1.class)
                .withInitializer(service1 -> Mockito.doReturn("spied").when(service1).get(11));
        TestSupport.build(DefaultTestApp.class)
                .hooks(hook, builder -> builder.extensions(Mng.class))
                .runCore(injector -> {
                    final Service1 spy = proxy.getSpy();

                    Mockito.verify(spy, Mockito.times(1)).get(11);

                    Assertions.assertEquals("spied", injector.getInstance(Mng.class).res);
                    return null;
                });
    }

    public static class Service1 {

        public String get(int id) {
            return "Hello " + id;
        }
    }

    @Singleton
    public static class Mng implements Managed {
        @Inject
        Service1 service1;

        public String res;

        @Override
        public void start() throws Exception {
            res = service1.get(11);
        }
    }
}
