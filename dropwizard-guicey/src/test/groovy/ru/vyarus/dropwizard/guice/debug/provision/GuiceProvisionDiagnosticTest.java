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
 * @since 24.03.2025
 */
public class GuiceProvisionDiagnosticTest extends AbstractPlatformTest {

    @Test
    void testDefaultOutput() {
        String out = run(Test1.class);
        Assertions.assertThat(out).contains("Guice bindings provision time: \n" +
                "\n" +
                "\tOverall 47 provisions took 111 ms \n" +
                "\t\tbinding              [@Singleton]     ManagedFilterPipeline                                                                 : 111 ms \t\t com.google.inject.servlet.InternalServletModule.configure(InternalServletModule.java:94)\n" +
                "\t\tbinding              [@Singleton]     ManagedServletPipeline                                                                : 111 ms \t\t com.google.inject.servlet.InternalServletModule.configure(InternalServletModule.java:95)");
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
                    .build();
        }
    }

    @Override
    protected String clean(String out) {
        return unifyMs(out);
    }
}
