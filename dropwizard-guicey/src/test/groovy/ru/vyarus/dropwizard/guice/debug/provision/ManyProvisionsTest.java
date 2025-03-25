package ru.vyarus.dropwizard.guice.debug.provision;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.AbstractPlatformTest;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.support.DefaultTestApp;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;

/**
 * @author Vyacheslav Rusakov
 * @since 25.03.2025
 */
public class ManyProvisionsTest extends AbstractPlatformTest {

    @Test
    void testMultipleProvisions() {
        String out = run(Test1.class);
        Assertions.assertThat(out).contains(
                "JIT                  [@Prototype]     JitService                                                                       x10  : 111 ms (111 ms + 111 ms + 111 ms + 111 ms + 111 ms + ...)");
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
                    .printGuiceProvisionTime()
                    .onGuiceyStartup((config, env, injector) -> {
                        for (int i = 0; i < 10; i++) {
                            injector.getInstance(JitService.class);
                        }
                    })
                    .build();
        }
    }

    public static class JitService {}

    @Override
    protected String clean(String out) {
        return unifyMs(out);
    }
}
