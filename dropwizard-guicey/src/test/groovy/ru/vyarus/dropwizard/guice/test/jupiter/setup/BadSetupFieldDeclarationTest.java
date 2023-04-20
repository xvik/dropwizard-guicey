package ru.vyarus.dropwizard.guice.test.jupiter.setup;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.platform.testkit.engine.EngineTestKit;
import ru.vyarus.dropwizard.guice.support.AutoScanApplication;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;
import ru.vyarus.dropwizard.guice.test.jupiter.env.EnableSetup;
import ru.vyarus.dropwizard.guice.test.jupiter.env.TestEnvironmentSetup;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

/**
 * @author Vyacheslav Rusakov
 * @since 17.05.2022
 */
public class BadSetupFieldDeclarationTest {

    @Test
    void checkIncorrectFieldDetection() {
        EngineTestKit
                .engine("junit-jupiter")
                .configurationParameter("junit.jupiter.conditions.deactivate", "org.junit.*DisabledCondition")
                .selectors(selectClass(BadSetupFieldDeclarationTest.Test1.class))
                .execute()
                .testEvents()
                .debug()
                .assertStatistics(stats -> stats.succeeded(0));
    }

    @TestGuiceyApp(AutoScanApplication.class)
    @Disabled // prevent direct execution
    public static class Test1 {

        // not a static field
        @EnableSetup
        TestEnvironmentSetup ext = it -> it.configOverrides("foo: 1");

        @Test
        void fooTest() {
        }
    }
}
