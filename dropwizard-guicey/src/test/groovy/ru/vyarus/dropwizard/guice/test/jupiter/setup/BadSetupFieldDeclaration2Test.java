package ru.vyarus.dropwizard.guice.test.jupiter.setup;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.platform.testkit.engine.EngineTestKit;
import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook;
import ru.vyarus.dropwizard.guice.support.AutoScanApplication;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;
import ru.vyarus.dropwizard.guice.test.jupiter.env.EnableSetup;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

/**
 * @author Vyacheslav Rusakov
 * @since 18.05.2022
 */
public class BadSetupFieldDeclaration2Test {

    @Test
    void checkIncorrectFieldDetection() {
        EngineTestKit
                .engine("junit-jupiter")
                .configurationParameter("junit.jupiter.conditions.deactivate", "org.junit.*DisabledCondition")
                .selectors(selectClass(Test1.class))
                .execute()
                .testEvents()
                .debug()
                .assertStatistics(stats -> stats.succeeded(0));
    }

    @TestGuiceyApp(AutoScanApplication.class)
    @Disabled // prevent direct execution
    public static class Test1 {

        // bad type
        @EnableSetup
        GuiceyConfigurationHook ext = it -> {};

        @Test
        void fooTest() {
        }
    }
}
