package ru.vyarus.dropwizard.guice.test.jupiter.guicey;

import io.dropwizard.core.Application;
import io.dropwizard.core.Configuration;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.testkit.engine.EngineTestKit;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;
import ru.vyarus.dropwizard.guice.test.builder.TestSupportHolder;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

/**
 * @author Vyacheslav Rusakov
 * @since 15.05.2020
 */
public class GuiceyExtensionShutdownTest {

    @Test
    void checkParallelExecution() {
        EngineTestKit
                .engine("junit-jupiter")
                .selectors(selectClass(Test1.class))
                .execute()
                .testEvents()
                .debug()
                .assertStatistics(stats -> stats.succeeded(1));

        Assertions.assertTrue(App.shutdown);
        Assertions.assertFalse(TestSupportHolder.isContextSet());
    }

    @TestGuiceyApp(App.class)
    public static class Test1 {

        @Test
        void check() {
        }
    }

    public static class App extends Application<Configuration> {

        public static boolean shutdown;

        @Override
        public void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder().build());
        }

        @Override
        public void run(Configuration configuration, Environment environment) throws Exception {
            environment.lifecycle().manage(new Managed() {
                @Override
                public void start() throws Exception {

                }

                @Override
                public void stop() throws Exception {
                    shutdown = true;
                }
            });
        }
    }
}
