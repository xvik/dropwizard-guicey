package ru.vyarus.dropwizard.guice.test.reuse;

import com.google.common.base.Preconditions;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.testkit.engine.EngineTestKit;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycleAdapter;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.jersey.ApplicationStartedEvent;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.jersey.ApplicationStoppedEvent;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.GuiceyExtensionsSupport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

/**
 * @author Vyacheslav Rusakov
 * @since 26.12.2022
 */
public class ExtensionsApiTest {
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

        Assertions.assertEquals(Arrays.asList(
                "started", "reusable1: true", "reusable1: true", "stopped", "reusable2: true",
                "Error: (IllegalStateException) Can't find guicey injector to process test fields injections"), actions);
        Assertions.assertEquals(1, App.cnt);
    }

    @TestGuiceyApp(value = App.class, reuseApplication = true)
    public abstract static class Base {
    }

    @ExtendWith(Test1Ext.class)
    @Disabled // prevent direct execution
    public static class Test1 extends Base {

        @Test
        void testSample() {
        }
    }

    public static class Test1Ext implements BeforeAllCallback, BeforeEachCallback {
        @Override
        public void beforeAll(ExtensionContext context) throws Exception {
            actions.add("reusable1: " + GuiceyExtensionsSupport.isReusableAppUsed(context));
        }

        @Override
        public void beforeEach(ExtensionContext context) throws Exception {
            actions.add("reusable1: " + GuiceyExtensionsSupport.isReusableAppUsed(context));
        }
    }

    @ExtendWith(Test2Ext.class)
    @Disabled // prevent direct execution
    public static class Test2 extends Base {

        @Test
        void testSample() {
        }
    }

    public static class Test2Ext implements BeforeAllCallback {
        @Override
        public void beforeAll(ExtensionContext context) throws Exception {
            Preconditions.checkState(GuiceyExtensionsSupport.isReusableAppUsed(context));
            Assertions.assertTrue(GuiceyExtensionsSupport.closeReusableApp(context));
            actions.add("reusable2: " + GuiceyExtensionsSupport.isReusableAppUsed(context));
            // still would be exception next because injector is required for fields injection
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
