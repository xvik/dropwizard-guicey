package ru.vyarus.dropwizard.guice.test.reuse;

import com.google.common.truth.Truth;
import io.dropwizard.core.Application;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.testkit.engine.EngineTestKit;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook;
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycleAdapter;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.jersey.ApplicationStartedEvent;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.jersey.ApplicationStoppedEvent;
import ru.vyarus.dropwizard.guice.test.EnableHook;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;
import ru.vyarus.dropwizard.guice.test.jupiter.env.EnableSetup;
import ru.vyarus.dropwizard.guice.test.jupiter.env.TestEnvironmentSetup;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;
import uk.org.webcompere.systemstubs.stream.SystemOut;

import java.util.ArrayList;
import java.util.List;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

/**
 * @author Vyacheslav Rusakov
 * @since 26.12.2022
 */
@ExtendWith(SystemStubsExtension.class)
public class IncorrectFieldsUsageTest {

    public static List<String> actions = new ArrayList<>();

    @SystemStub
    SystemOut out;

    @Test
    void checkNotAbstractBase() {
        EngineTestKit
                .engine("junit-jupiter")
                .configurationParameter("junit.jupiter.conditions.deactivate", "org.junit.*DisabledCondition")
                .selectors(
                        selectClass(Test1.class),
                        selectClass(Test2.class)
                )
                .execute().allEvents().failed().stream()
                // exceptions appended to events log
                .forEach(event -> {
                    Throwable err = event.getPayload(TestExecutionResult.class).get().getThrowable().get();
                    actions.add("Error: (" + err.getClass().getSimpleName() + ") " + err.getMessage());
                });

        Assertions.assertEquals(2, actions.size());

        String output = out.getText().replace("\r", "");
        System.err.println(output);
        Truth.assertThat(output).contains("The following extensions were used during reusable app startup in test ru.vyarus.dropwizard.guice.test.reuse.IncorrectFieldsUsageTest$Test1, but they did not belong to base class ru.vyarus.dropwizard.guice.test.reuse.IncorrectFieldsUsageTest$Base hierarchy where reusable app is declared and so would be ignored if reusable app would start by different test: \n" +
                "\tru.vyarus.dropwizard.guice.test.reuse.IncorrectFieldsUsageTest$Test1.kohook (GuiceyConfigurationHook)\n" +
                "\tru.vyarus.dropwizard.guice.test.reuse.IncorrectFieldsUsageTest$Test1.kosetup (TestEnvironmentSetup)");

        Truth.assertThat(output).contains("The following extensions were ignored in test ru.vyarus.dropwizard.guice.test.reuse.IncorrectFieldsUsageTest$Test2 because reusable application was already started by another test: \n" +
                "\tru.vyarus.dropwizard.guice.test.reuse.IncorrectFieldsUsageTest$Test2.ignoredhook (GuiceyConfigurationHook)\n" +
                "\tru.vyarus.dropwizard.guice.test.reuse.IncorrectFieldsUsageTest$Test2.ignoredsetup (TestEnvironmentSetup)");
    }

    @TestGuiceyApp(value = App.class, reuseApplication = true)
    public abstract static class Base {

        @EnableSetup
        static TestEnvironmentSetup oksetup = extension -> null;
        @EnableHook
        static GuiceyConfigurationHook okhook = builder -> {};
    }

    @Disabled // prevent direct execution
    public static class Test1 extends Base {

        @EnableSetup
        static TestEnvironmentSetup kosetup = extension -> null;
        @EnableHook
        static GuiceyConfigurationHook kohook = builder -> {};

        @Test
        void testSample() {
        }
    }


    @Disabled // prevent direct execution
    public static class Test2 extends Base {

        @EnableSetup
        static TestEnvironmentSetup ignoredsetup = extension -> null;
        @EnableHook
        static GuiceyConfigurationHook ignoredhook = builder -> {};

        @Test
        void testSample() {
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
