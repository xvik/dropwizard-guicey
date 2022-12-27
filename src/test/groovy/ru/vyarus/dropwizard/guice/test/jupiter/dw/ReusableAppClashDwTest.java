package ru.vyarus.dropwizard.guice.test.jupiter.dw;

import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.testkit.engine.EngineTestKit;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycleAdapter;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.jersey.ApplicationStartedEvent;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.jersey.ApplicationStoppedEvent;
import ru.vyarus.dropwizard.guice.test.ClientSupport;
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

/**
 * @author Vyacheslav Rusakov
 * @since 27.12.2022
 */
public class ReusableAppClashDwTest {

    public static List<String> actions = new ArrayList<>();

    @Test
    void checkAppReuse() {
        EngineTestKit
                .engine("junit-jupiter")
                .configurationParameter("junit.jupiter.conditions.deactivate", "org.junit.*DisabledCondition")
                .selectors(
                        selectClass(Test1.class),
                        selectClass(Test2.class))
                .execute().allEvents().failed().stream()
                // exceptions appended to events log
                .forEach(event -> {
                    Throwable err = event.getPayload(TestExecutionResult.class).get().getThrowable().get();
                    actions.add("Error: (" + err.getClass().getSimpleName() + ") " + err.getMessage());
                });

        Assertions.assertEquals(Arrays.asList("started", "started", "stopped", "stopped"), actions);
        Assertions.assertEquals(2, App.cnt);
    }

    @TestDropwizardApp(value = App.class, reuseApplication = true)
    public abstract static class Base {
    }

    @Disabled // prevent direct execution
    public static class Test1 extends Base {

        @Test
        void testSample() {
        }
    }

    @Disabled // prevent direct execution
    @TestDropwizardApp(value = App.class, randomPorts = true)
    public static class Test2 {

        @Test
        void testSample(ClientSupport client) {

            // test-own app
            Assertions.assertEquals(200, client.targetAdmin("/ping").request().get().getStatus());
            // shared app instance
            Assertions.assertEquals(200, client.target("http://localhost:8081/ping").request().get().getStatus());
        }
    }

    public static class App extends Application<Configuration> {

        public static int cnt;

        @Override
        public void initialize(Bootstrap<Configuration> bootstrap) {
            cnt++;
            bootstrap.addBundle(GuiceBundle.builder()
                    .listen(new GuiceyLifecycleAdapter() {
                        @Override
                        protected void applicationStarted(ApplicationStartedEvent event) {
                            actions.add("started");
                        }

                        @Override
                        protected void applicationStopped(ApplicationStoppedEvent event) {
                            actions.add("stopped");
                        }
                    })
                    .build());
        }

        @Override
        public void run(Configuration configuration, Environment environment) throws Exception {

        }
    }
}
