package ru.vyarus.dropwizard.guice.test.jupiter.setup;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.platform.testkit.engine.EngineTestKit;
import ru.vyarus.dropwizard.guice.support.AutoScanApplication;
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;
import ru.vyarus.dropwizard.guice.test.jupiter.env.EnableSetup;
import ru.vyarus.dropwizard.guice.test.jupiter.env.TestEnvironmentSetup;
import ru.vyarus.dropwizard.guice.test.jupiter.env.TestExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

/**
 * @author Vyacheslav Rusakov
 * @since 17.05.2022
 */
public class SetupRecognitionTest {

    public static List<String> actions = new ArrayList<>();

    @Test
    void checkSetupRecognition() {
        EngineTestKit
                .engine("junit-jupiter")
                .configurationParameter("junit.jupiter.conditions.deactivate", "org.junit.*DisabledCondition")
                .selectors(
                        selectClass(SetupRecognitionTest.TestG.class),
                        selectClass(SetupRecognitionTest.TestD.class))
                .execute()
                .testEvents()
                .debug()
                .assertStatistics(stats -> stats.succeeded(2));

        Assertions.assertEquals(Arrays.asList("setup", "base", "testg", "setup", "base", "testd"), actions);
    }

    public static class Setup implements TestEnvironmentSetup {
        @Override
        public Object setup(TestExtension extension) {
            actions.add("setup");
            return null;
        }
    }

    public static class Base {

        @EnableSetup
        static TestEnvironmentSetup base = it -> actions.add("base");
    }

    @TestGuiceyApp(value = AutoScanApplication.class, setup = Setup.class)
    @Disabled // prevent direct execution
    public static class TestG extends Base {

        @EnableSetup
        static TestEnvironmentSetup setup = it -> actions.add("testg");

        @Test
        void fooTest() {
        }
    }

    @TestDropwizardApp(value = AutoScanApplication.class, setup = Setup.class)
    @Disabled // prevent direct execution
    public static class TestD extends Base {

        @EnableSetup
        static TestEnvironmentSetup setup = it -> actions.add("testd");

        @Test
        void fooTest() {
        }
    }
}
