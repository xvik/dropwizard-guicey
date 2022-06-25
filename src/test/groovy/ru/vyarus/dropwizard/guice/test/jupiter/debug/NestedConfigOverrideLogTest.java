package ru.vyarus.dropwizard.guice.test.jupiter.debug;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.platform.testkit.engine.EngineTestKit;
import ru.vyarus.dropwizard.guice.support.AutoScanApplication;
import ru.vyarus.dropwizard.guice.test.TestSupport;
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
public class NestedConfigOverrideLogTest {


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
                .assertStatistics(stats -> stats.succeeded(2));

        String output = out.getText().replace("\r", "");
        System.err.println(output);

        Assertions.assertTrue(output.contains("Guicey test extensions (Test1.Inner.test1.):"));
        Assertions.assertTrue(output.contains("Applied configuration overrides (Test1.Inner.test1.): \n" +
                "\n" +
                "\t                  foo = 1"));

        Assertions.assertTrue(output.contains("Guicey test extensions (Test1.Inner.test2.):"));
        Assertions.assertTrue(output.contains("Applied configuration overrides (Test1.Inner.test2.): \n" +
                "\n" +
                "\t                  foo = 2"));
    }

    public static class Test1 {
        static int i = 1;

        @RegisterExtension
        TestGuiceyAppExtension extension = TestGuiceyAppExtension.forApp(AutoScanApplication.class)
                // setup object used only to check log
                .setup(ext -> ext.configOverride("foo", ()-> String.valueOf(i++)))
                .debug()
                .create();

        @Nested
        class Inner {

            @Test
            void test1() {}

            @Test
            void test2() {}
        }
    }
}
