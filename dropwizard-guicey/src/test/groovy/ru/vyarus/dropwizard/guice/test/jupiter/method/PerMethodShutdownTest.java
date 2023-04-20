package ru.vyarus.dropwizard.guice.test.jupiter.method;

import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.platform.testkit.engine.EngineTestKit;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycleAdapter;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.jersey.ApplicationStartedEvent;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.jersey.ApplicationStoppedEvent;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.TestGuiceyAppExtension;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

/**
 * @author Vyacheslav Rusakov
 * @since 04.06.2022
 */
public class PerMethodShutdownTest {

    public static int starts = 0;
    public static int stops = 0;

    @Test
    void testShutdown() {
        EngineTestKit.engine("junit-jupiter")
                .configurationParameter("junit.jupiter.conditions.deactivate", "org.junit.*DisabledCondition")
                .selectors(selectClass(TestShutdown.class))
                .execute()
                .testEvents()
                .debug()
                .assertStatistics(stats -> stats.succeeded(2));

        Assertions.assertEquals(2, starts);
        Assertions.assertEquals(2, stops);
    }

    @Disabled // prevent direct execution
    public static class TestShutdown {
        @RegisterExtension
        TestGuiceyAppExtension extension = TestGuiceyAppExtension.forApp(App.class)
                .create();

        @Test
        void test1() {
        }

        @Test
        void test2() {
        }
    }

    public static class App extends Application<Configuration> {

        @Override
        public void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .listen(new GuiceyLifecycleAdapter() {

                        @Override
                        protected void applicationStarted(ApplicationStartedEvent event) {
                            PerMethodShutdownTest.starts++;
                        }

                        @Override
                        protected void applicationStopped(ApplicationStoppedEvent event) {
                            PerMethodShutdownTest.stops++;
                        }
                    })
                    .build());
        }

        @Override
        public void run(Configuration configuration, Environment environment) throws Exception {
        }
    }
}
