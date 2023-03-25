package ru.vyarus.dropwizard.guice.test.jupiter.method;

import io.dropwizard.core.Application;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.platform.testkit.engine.EngineTestKit;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook;
import ru.vyarus.dropwizard.guice.test.EnableHook;
import ru.vyarus.dropwizard.guice.test.jupiter.env.EnableSetup;
import ru.vyarus.dropwizard.guice.test.jupiter.env.TestEnvironmentSetup;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.TestGuiceyAppExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

/**
 * @author Vyacheslav Rusakov
 * @since 04.06.2022
 */
public class PerMethodSupportFieldsTest {

    static List<String> fields = new ArrayList<>();

    @Test
    void testNonStaticFields() {

        EngineTestKit.engine("junit-jupiter")
                .configurationParameter("junit.jupiter.conditions.deactivate", "org.junit.*DisabledCondition")
                .selectors(selectClass(FieldsTest.class))
                .execute()
                .testEvents()
                .debug()
                .assertStatistics(stats -> stats.succeeded(2));

        Assertions.assertEquals(Arrays.asList(
                "ssetup", "setup", "shook", "hook", "ssetup", "setup", "shook", "hook"), fields);
    }

    @Disabled // prevent direct execution
    public static class FieldsTest {
        @EnableHook
        static GuiceyConfigurationHook shook = it -> fields.add("shook");

        @EnableHook
        GuiceyConfigurationHook hook = it -> fields.add("hook");

        @EnableSetup
        static TestEnvironmentSetup ssetup = it -> fields.add("ssetup");

        @EnableSetup
        TestEnvironmentSetup setup = it -> fields.add("setup");

        @RegisterExtension
        TestGuiceyAppExtension ext = TestGuiceyAppExtension.forApp(App.class).create();

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
            bootstrap.addBundle(GuiceBundle.builder().build());
        }

        @Override
        public void run(Configuration configuration, Environment environment) throws Exception {
        }
    }
}
