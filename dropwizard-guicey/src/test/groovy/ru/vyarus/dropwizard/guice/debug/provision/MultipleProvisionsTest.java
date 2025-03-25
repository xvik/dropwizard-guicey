package ru.vyarus.dropwizard.guice.debug.provision;

import jakarta.inject.Inject;
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
public class MultipleProvisionsTest extends AbstractPlatformTest {

    @Test
    void testMultipleProvisions() {
        String out = run(Test1.class);
        Assertions.assertThat(out).contains(
                "JIT                  [@Prototype]     JitService                                                                       x2   : 111 ms (111 ms + 111 ms )");
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
                    .extensions(Service1.class, Service2.class)
                    .printGuiceProvisionTime()
                    .build();
        }
    }

    public static class JitService {}

    @EagerSingleton
    public static class Service1 {

        @Inject
        JitService service;
    }

    @EagerSingleton
    public static class Service2 {

        @Inject
        JitService service;
    }

    @Override
    protected String clean(String out) {
        return unifyMs(out);
    }
}
