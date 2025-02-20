package ru.vyarus.dropwizard.guice.test.jupiter.setup;

import com.google.common.base.Preconditions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.testkit.engine.EngineTestKit;
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo;
import ru.vyarus.dropwizard.guice.support.DefaultTestApp;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;
import ru.vyarus.dropwizard.guice.test.jupiter.env.TestEnvironmentSetup;
import ru.vyarus.dropwizard.guice.test.jupiter.env.TestExtension;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.TestGuiceyAppExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

/**
 * @author Vyacheslav Rusakov
 * @since 20.02.2025
 */
public class ListenerLambdaTest {
    public static List<String> actions = new ArrayList<>();


    @Test
    void checkListeners() {
        actions.clear();
        EngineTestKit
                .engine("junit-jupiter")
                .configurationParameter("junit.jupiter.conditions.deactivate", "org.junit.*DisabledCondition")
                .selectors(selectClass(Test1.class))
                .execute().allEvents().failed().stream()
                // exceptions appended to events log
                .forEach(event -> {
                    Throwable err = event.getPayload(TestExecutionResult.class).get().getThrowable().get();
                    err.printStackTrace();
                    actions.add("Error: (" + err.getClass().getSimpleName() + ") " + err.getMessage());
                });

        Assertions.assertEquals(Arrays.asList(
                "started", "beforeAll", "beforeEach", "afterEach", "beforeEach", "afterEach", "stopped", "afterAll"), actions);
    }


    @Test
    void checkListenersForPerMethod() {
        actions.clear();
        EngineTestKit
                .engine("junit-jupiter")
                .configurationParameter("junit.jupiter.conditions.deactivate", "org.junit.*DisabledCondition")
                .selectors(selectClass(Test2.class))
                .execute().allEvents().failed().stream()
                // exceptions appended to events log
                .forEach(event -> {
                    Throwable err = event.getPayload(TestExecutionResult.class).get().getThrowable().get();
                    err.printStackTrace();
                    actions.add("Error: (" + err.getClass().getSimpleName() + ") " + err.getMessage());
                });

        Assertions.assertEquals(Arrays.asList(
                "started", "beforeEach", "stopped", "afterEach", "started", "beforeEach", "stopped", "afterEach"), actions);
    }

    @TestGuiceyApp(value = DefaultTestApp.class, debug = true, setup = Setup.class)
    @Disabled // prevent direct execution
    public static class Test1 {

        @Test
        void fooTest() {
        }

        @Test
        void fooTest2() {
        }
    }

    @Disabled // prevent direct execution
    public static class Test2 {

        @RegisterExtension
        TestGuiceyAppExtension ext = TestGuiceyAppExtension.forApp(DefaultTestApp.class)
                .setup(Setup.class)
                .debug()
                .create();

        @Test
        void fooTest() {
        }

        @Test
        void fooTest2() {
        }
    }

    public static class Setup implements TestEnvironmentSetup {
        @Override
        public Object setup(TestExtension extension) {
            return extension
                    .onApplicationStart(context -> {
                        Preconditions.checkNotNull(context);
                        Preconditions.checkNotNull(context.getJunitContext());
                        Preconditions.checkNotNull(context.getSupport());
                        Preconditions.checkNotNull(context.getInjector());
                        Preconditions.checkNotNull(context.getClient());
                        Preconditions.checkNotNull(context.getBean(GuiceyConfigurationInfo.class).getActiveScopes());
                        actions.add("started");
                    })
                    .onBeforeAll(context -> {
                        Preconditions.checkNotNull(context);
                        Preconditions.checkNotNull(context.getJunitContext());
                        Preconditions.checkNotNull(context.getSupport());
                        Preconditions.checkNotNull(context.getInjector());
                        Preconditions.checkNotNull(context.getClient());
                        actions.add("beforeAll");
                    })
                    .onBeforeEach(context -> {
                        Preconditions.checkNotNull(context);
                        Preconditions.checkNotNull(context.getJunitContext());
                        Preconditions.checkNotNull(context.getSupport());
                        Preconditions.checkNotNull(context.getInjector());
                        Preconditions.checkNotNull(context.getClient());
                        actions.add("beforeEach");
                    })
                    .onAfterEach(context -> {
                        Preconditions.checkNotNull(context);
                        Preconditions.checkNotNull(context.getJunitContext());
                        Preconditions.checkNotNull(context.getSupport());
                        Preconditions.checkNotNull(context.getInjector());
                        Preconditions.checkNotNull(context.getClient());
                        actions.add("afterEach");
                    })
                    .onAfterAll(context -> {
                        Preconditions.checkNotNull(context);
                        Preconditions.checkNotNull(context.getJunitContext());
                        Preconditions.checkNotNull(context.getSupport());
                        Preconditions.checkNotNull(context.getInjector());
                        Preconditions.checkNotNull(context.getClient());
                        actions.add("afterAll");
                    })
                    .onApplicationStop(context -> {
                        Preconditions.checkNotNull(context);
                        Preconditions.checkNotNull(context.getJunitContext());
                        Preconditions.checkNotNull(context.getSupport());
                        Preconditions.checkNotNull(context.getInjector());
                        Preconditions.checkNotNull(context.getClient());
                        actions.add("stopped");
                    });
        }
    }
}
