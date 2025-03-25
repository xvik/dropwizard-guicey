package ru.vyarus.dropwizard.guice.debug.provision;

import com.google.inject.Inject;
import com.google.inject.TypeLiteral;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.AbstractPlatformTest;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.module.installer.feature.eager.EagerSingleton;
import ru.vyarus.dropwizard.guice.support.DefaultTestApp;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;

/**
 * @author Vyacheslav Rusakov
 * @since 25.03.2025
 */
public class GenerifiedBindingsTest extends AbstractPlatformTest {

    @Test
    void testGenerifiedServices() {
        String out = run(Test1.class);
        Assertions.assertThat(out).contains("Possible mistakes (unqualified JIT bindings):\n" +
                "\n" +
                "\t\t @Inject Service:\n" +
                "\t\t\t  instance             [@Singleton]     GenerifiedBindingsTest.Service<Integer>                                               : 111 ms \t\t ru.vyarus.dropwizard.guice.debug.provision.GenerifiedBindingsTest$App.lambda$configure$0(GenerifiedBindingsTest.java:46)\n" +
                "\t\t\t  instance             [@Singleton]     GenerifiedBindingsTest.Service<String>                                                : 111 ms \t\t ru.vyarus.dropwizard.guice.debug.provision.GenerifiedBindingsTest$App.lambda$configure$0(GenerifiedBindingsTest.java:45)\n" +
                "\t\t\t> JIT                  [@Prototype]     Service                                                                               : 111 ms");
    }

    @TestGuiceyApp(App.class)
    @Disabled
    public static class Test1 {
        @Test
        void test() {
        }
    }

    public static class App extends DefaultTestApp {
        @Override
        protected GuiceBundle configure() {
            return GuiceBundle.builder()
                    .extensions(Sample.class)
                    .modules(binder -> {
                        binder.bind(new TypeLiteral<Service<String>>() {}).toInstance(new Service<>() {});
                        binder.bind(new TypeLiteral<Service<Integer>>() {}).toInstance(new Service<>() {});
                    })
                    .printGuiceProvisionTime()
                    .build();
        }
    }

    public static class Service<T> {}

    @EagerSingleton
    public static class Sample {

        @Inject
        Service<Integer> serviceI;
        @Inject
        Service<String> serviceS;
        @Inject
        Service service;
    }

    @Override
    protected String clean(String out) {
        return unifyMs(out);
    }
}
