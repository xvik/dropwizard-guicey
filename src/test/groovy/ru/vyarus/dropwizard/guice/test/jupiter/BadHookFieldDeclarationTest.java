package ru.vyarus.dropwizard.guice.test.jupiter;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.platform.testkit.engine.EngineTestKit;
import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook;
import ru.vyarus.dropwizard.guice.support.AutoScanApplication;
import ru.vyarus.dropwizard.guice.support.feature.DummyResource;
import ru.vyarus.dropwizard.guice.test.EnableHook;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

/**
 * @author Vyacheslav Rusakov
 * @since 26.05.2020
 */
public class BadHookFieldDeclarationTest {

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

        // not a static field
        @EnableHook
        GuiceyConfigurationHook hook = it -> it.disableExtensions(DummyResource.class);

        @Test
        void fooTest() {
        }
    }
}
