package ru.vyarus.dropwizard.guice.test.jupiter.setup.spy;

import com.google.inject.Inject;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.support.DefaultTestApp;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.spy.SpyBean;

/**
 * @author Vyacheslav Rusakov
 * @since 17.02.2025
 */
public class InstanceSpyDetectionTest extends AbstractSpyTest {

    @Test
    void testInstanceSpyDetection() {

        Throwable th = runFailed(Test1.class);

        Assertions.assertThat(th.getMessage()).isEqualTo(
                "Incorrect @SpyBean 'InstanceSpyDetectionTest$Test1.spy' declaration: target bean 'Service' " +
                        "bound by instance and so can't be spied");
    }

    @TestGuiceyApp(Test1.App.class)
    @Disabled
    public static class Test1 {
        @Inject
        Test1.Service service;

        @SpyBean
        Test1.Service spy;

        @Test
        void testInstanceTracking() {
            // nothing
        }

        public static class App extends DefaultTestApp {

            @Override
            protected GuiceBundle configure() {
                return GuiceBundle.builder()
                        .modules(builder -> builder.bind(Test1.Service.class).toInstance(new Test1.Service()))
                        .build();
            }
        }

        public static class Service  {

            public String foo(int i) {
                return "foo" + i;
            }
        }
    }
}
