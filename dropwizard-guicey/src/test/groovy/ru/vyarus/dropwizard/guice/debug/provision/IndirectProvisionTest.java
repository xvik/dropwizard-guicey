package ru.vyarus.dropwizard.guice.debug.provision;

import javax.inject.Inject;
import javax.inject.Provider;
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
public class IndirectProvisionTest extends AbstractPlatformTest {

    @Test
    void testIndirectProvisionCounter() {
        String out = run(Test1.class);
        Assertions.assertThat(out).contains(
                "JIT                  [@Prototype]     Service                                                                          x2   : 111 ms (111 ms + 111 ms )");
        Assertions.assertThat(out).contains(
                "providerinstance     [@Prototype]     Dep                                                                              x2   : 111 ms (111 ms + 111 ms )");
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
                    .extensions(Ext1.class, Ext2.class)
                    .modules(binder -> binder.bind(Dep.class).toProvider((Provider<Dep>) Dep::new))
                    .printGuiceProvisionTime()
                    .build();
        }
    }

    public static class Dep {}

    public static class Service {
        @Inject
        Dep dep;
    }

    @EagerSingleton
    public static class Ext1 {
        @Inject
        Service service;
    }

    @EagerSingleton
    public static class Ext2 {
        @Inject
        Service service;
    }

    @Override
    protected String clean(String out) {
        return unifyMs(out);
    }
}
