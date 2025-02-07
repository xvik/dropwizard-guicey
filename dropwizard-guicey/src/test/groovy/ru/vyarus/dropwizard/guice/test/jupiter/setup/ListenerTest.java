package ru.vyarus.dropwizard.guice.test.jupiter.setup;

import com.google.common.base.Preconditions;
import io.dropwizard.core.Application;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.testkit.engine.EngineTestKit;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;
import ru.vyarus.dropwizard.guice.test.jupiter.env.TestEnvironmentSetup;
import ru.vyarus.dropwizard.guice.test.jupiter.env.TestExecutionListener;
import ru.vyarus.dropwizard.guice.test.jupiter.env.TestExtension;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.TestGuiceyAppExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

/**
 * @author Vyacheslav Rusakov
 * @since 07.02.2025
 */
public class ListenerTest {
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



    public static class App extends Application<Configuration> {

        @Override
        public void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder().build());
        }

        @Override
        public void run(Configuration configuration, Environment environment) throws Exception {
        }
    }

    @TestGuiceyApp(value = App.class, debug = true, setup = Setup.class)
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
        TestGuiceyAppExtension ext = TestGuiceyAppExtension.forApp(App.class)
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
           extension.listen(new TestExecutionListener() {
               @Override
               public void started(ExtensionContext context) {
                   Preconditions.checkNotNull(context);
                   getSupport(context);
                   getInjector(context);
                   getClient(context);
                   getBean(context, GuiceyConfigurationInfo.class).getActiveScopes();
                   actions.add("started");
               }

               @Override
               public void beforeAll(ExtensionContext context) {
                   Preconditions.checkNotNull(context);
                   getSupport(context);
                   getInjector(context);
                   getClient(context);
                   actions.add("beforeAll");
               }

               @Override
               public void beforeEach(ExtensionContext context) {
                   Preconditions.checkNotNull(context);
                   getSupport(context);
                   getInjector(context);
                   getClient(context);
                   actions.add("beforeEach");
               }

               @Override
               public void afterEach(ExtensionContext context) {
                   Preconditions.checkNotNull(context);
                   getSupport(context);
                   getInjector(context);
                   getClient(context);
                   actions.add("afterEach");
               }

               @Override
               public void afterAll(ExtensionContext context) {
                   Preconditions.checkNotNull(context);
                   getSupport(context);
                   getInjector(context);
                   getClient(context);
                   actions.add("afterAll");
               }

               @Override
               public void stopped(ExtensionContext context) {
                   Preconditions.checkNotNull(context);
                   getSupport(context);
                   getInjector(context);
                   getClient(context);
                   actions.add("stopped");
               }
           });
           return null;
       }
   }
}
