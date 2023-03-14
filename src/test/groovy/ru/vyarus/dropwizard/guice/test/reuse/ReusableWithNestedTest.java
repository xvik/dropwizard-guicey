package ru.vyarus.dropwizard.guice.test.reuse;

import io.dropwizard.core.Application;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.testkit.engine.EngineTestKit;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycleAdapter;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.jersey.ApplicationStartedEvent;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.jersey.ApplicationStoppedEvent;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

/**
 * @author Vyacheslav Rusakov
 * @since 26.12.2022
 */
public class ReusableWithNestedTest {
    public static List<String> actions = new ArrayList<>();

    @Test
    void checkNotAbstractBase() {
        EngineTestKit
                .engine("junit-jupiter")
                .configurationParameter("junit.jupiter.conditions.deactivate", "org.junit.*DisabledCondition")
                .selectors(
                        selectClass(Test1.class)
                )
                .execute().allEvents().failed().stream()
                // exceptions appended to events log
                .forEach(event -> {
                    Throwable err = event.getPayload(TestExecutionResult.class).get().getThrowable().get();
                    actions.add("Error: (" + err.getClass().getSimpleName() + ") " + err.getMessage());
                });

        Assertions.assertEquals(Arrays.asList("started", "stopped"), actions);
    }

    @TestGuiceyApp(value = App.class, reuseApplication = true)
    public abstract static class Base {
    }

    @Disabled // prevent direct execution
    public static class Test1 extends Base {

        @Test
        void testSample() {
        }

        @Nested
        class Level1 {

            @Inject
            Environment environment;

            @Test
            void checkExtensionApplied() {
                Assertions.assertNotNull(environment);
            }

            @Nested
            class Level2 {
                @Inject
                Environment env;

                @Test
                void checkExtensionApplied() {
                    Assertions.assertNotNull(env);
                }

                @Nested
                class Level3 {

                    @Inject
                    Environment envr;

                    @Test
                    void checkExtensionApplied() {
                        Assertions.assertNotNull(envr);
                    }
                }
            }
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
