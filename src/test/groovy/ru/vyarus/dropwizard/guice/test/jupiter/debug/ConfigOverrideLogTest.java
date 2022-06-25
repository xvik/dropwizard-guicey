package ru.vyarus.dropwizard.guice.test.jupiter.debug;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.platform.testkit.engine.EngineTestKit;
import ru.vyarus.dropwizard.guice.support.AutoScanApplication;
import ru.vyarus.dropwizard.guice.test.TestSupport;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.TestGuiceyAppExtension;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;
import uk.org.webcompere.systemstubs.stream.SystemOut;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

/**
 * @author Vyacheslav Rusakov
 * @since 25.06.2022
 */
@ExtendWith(SystemStubsExtension.class)
public class ConfigOverrideLogTest {

    @SystemStub
    SystemOut out;

    @Test
    void checkSetupOutputForAnnotation() {
        TestSupport.debugExtensions();
        EngineTestKit
                .engine("junit-jupiter")
                .configurationParameter("junit.jupiter.conditions.deactivate", "org.junit.*DisabledCondition")
                .selectors(selectClass(Test1.class))
                .execute()
                .testEvents()
                .debug()
                .assertStatistics(stats -> stats.succeeded(1));

        String output = out.getText().replace("\r", "");
        System.err.println(output);

        Assertions.assertTrue(output.contains("Applied configuration overrides (Test1.): \n" +
                "\n" +
                "\t                  foo = 1"));
    }

    @Test
    void checkSetupOutputForManualRegistration() {
        TestSupport.debugExtensions();
        EngineTestKit
                .engine("junit-jupiter")
                .configurationParameter("junit.jupiter.conditions.deactivate", "org.junit.*DisabledCondition")
                .selectors(selectClass(Test2.class))
                .execute()
                .testEvents()
                .debug()
                .assertStatistics(stats -> stats.succeeded(1));

        String output = out.getText().replace("\r", "");
        System.err.println(output);

        Assertions.assertTrue(output.contains("Applied configuration overrides (Test2.): \n" +
                "\n" +
                "\t                  foo = 2"));
    }

    @Disabled // prevent direct execution
    @TestGuiceyApp(value = AutoScanApplication.class, configOverride = "foo: 1", debug = true)
    public static class Test1 {

        @Test
        void test() {
        }
    }

    @Disabled // prevent direct execution
    public static class Test2 {

        @RegisterExtension
        static TestGuiceyAppExtension app = TestGuiceyAppExtension.forApp(AutoScanApplication.class)
                .configOverrides("foo: 2")
                .debug()
                .create();

        @Test
        void test() {
        }

    }
}
