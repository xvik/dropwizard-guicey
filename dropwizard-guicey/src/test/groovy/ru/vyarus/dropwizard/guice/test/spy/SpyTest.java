package ru.vyarus.dropwizard.guice.test.spy;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ru.vyarus.dropwizard.guice.support.DefaultTestApp;
import ru.vyarus.dropwizard.guice.test.TestSupport;

/**
 * @author Vyacheslav Rusakov
 * @since 04.05.2025
 */
public class SpyTest {

    @Test
    void testSpy() throws Exception {
        SpiesHook hook = new SpiesHook();
        final SpyProxy<Service1> proxy = hook.spy(Service1.class);
        TestSupport.build(DefaultTestApp.class)
                .hooks(hook)
                .runCore(injector -> {
                    final Service1 spy = proxy.getSpy();
                    Mockito.doReturn("bar1").when(spy).get(11);

                    Service1 s1 = injector.getInstance(Service1.class);
                    Assertions.assertEquals("bar1", s1.get(11));
                    Assertions.assertEquals("Hello 10", s1.get(10));

                    Mockito.verify(spy, Mockito.times(1)).get(11);
                    Mockito.verify(spy, Mockito.times(1)).get(10);
                    
                    Assertions.assertEquals(spy, hook.getSpy(Service1.class));
                    Assertions.assertEquals(spy, proxy.get());

                    hook.resetSpies();
                    Assertions.assertEquals("Hello 11", s1.get(11));
                    return null;
                });
    }

    public static class Service1 {

        public String get(int id) {
            return "Hello " + id;
        }
    }

}
