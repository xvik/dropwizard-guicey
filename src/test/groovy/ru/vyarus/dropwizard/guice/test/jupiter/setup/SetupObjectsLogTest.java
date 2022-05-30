package ru.vyarus.dropwizard.guice.test.jupiter.setup;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.platform.testkit.engine.EngineTestKit;
import ru.vyarus.dropwizard.guice.support.AutoScanApplication;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;
import ru.vyarus.dropwizard.guice.test.jupiter.env.EnableSetup;
import ru.vyarus.dropwizard.guice.test.jupiter.env.TestEnvironmentSetup;
import ru.vyarus.dropwizard.guice.test.jupiter.env.TestExtension;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.TestGuiceyAppExtension;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;
import uk.org.webcompere.systemstubs.stream.SystemOut;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

/**
 * @author Vyacheslav Rusakov
 * @since 29.05.2022
 */
@ExtendWith(SystemStubsExtension.class)
public class SetupObjectsLogTest {

    @SystemStub
    SystemOut out;

    @Test
    void checkSetupOutputForAnnotation() {
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

        Assertions.assertTrue(output.replaceAll("\\$\\$Lambda\\$\\d+/\\d+(x[a-z\\d]+)?", "\\$\\$Lambda\\$111/1111111")
                .replaceAll("\\) {8,}\t", ")        \t")
                .contains("Guicey test extensions:\n" +
                        "\n" +
                        "\tSetup objects = \n" +
                        "\t\tExt1                         (r.v.d.g.t.j.s.SetupObjectsLogTest)        \t@TestGuiceyApp\n" +
                        "\t\tExt2                         (r.v.d.g.t.j.s.SetupObjectsLogTest)        \t@TestGuiceyApp\n" +
                        "\t\tSetupObjectsLogTest$Base$$Lambda$111/1111111 (r.v.d.g.t.j.setup)        \t@EnableSetup field Base.base1\n" +
                        "\t\tSetupObjectsLogTest$Base$$Lambda$111/1111111 (r.v.d.g.t.j.setup)        \t@EnableSetup field Base.base2\n" +
                        "\t\tSetupObjectsLogTest$Test1$$Lambda$111/1111111 (r.v.d.g.t.j.setup)        \t@EnableSetup field Test1.ext1\n" +
                        "\t\tSetupObjectsLogTest$Test1$$Lambda$111/1111111 (r.v.d.g.t.j.setup)        \t@EnableSetup field Test1.ext2\n"));
    }

    @Test
    void checkSetupOutputForManualRegistration() {
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

        Assertions.assertTrue(output.replaceAll("\\$\\$Lambda\\$\\d+/\\d+(x[a-z\\d]+)?", "\\$\\$Lambda\\$111/1111111")
                .replaceAll("\\) {8,}\t", ")        \t")
                .contains("Guicey test extensions:\n" +
                        "\n" +
                        "\tSetup objects = \n" +
                        "\t\tExt1                         (r.v.d.g.t.j.s.SetupObjectsLogTest)        \t@RegisterExtension class\n" +
                        "\t\tExt2                         (r.v.d.g.t.j.s.SetupObjectsLogTest)        \t@RegisterExtension class\n" +
                        "\t\tSetupObjectsLogTest$Test2$$Lambda$111/1111111 (r.v.d.g.t.j.setup)        \t@RegisterExtension instance\n" +
                        "\t\tSetupObjectsLogTest$Test2$$Lambda$111/1111111 (r.v.d.g.t.j.setup)        \t@RegisterExtension instance\n" +
                        "\t\tSetupObjectsLogTest$Base$$Lambda$111/1111111 (r.v.d.g.t.j.setup)        \t@EnableSetup field Base.base1\n" +
                        "\t\tSetupObjectsLogTest$Base$$Lambda$111/1111111 (r.v.d.g.t.j.setup)        \t@EnableSetup field Base.base2\n" +
                        "\t\tSetupObjectsLogTest$Test2$$Lambda$111/1111111 (r.v.d.g.t.j.setup)        \t@EnableSetup field Test2.ext1\n" +
                        "\t\tSetupObjectsLogTest$Test2$$Lambda$111/1111111 (r.v.d.g.t.j.setup)        \t@EnableSetup field Test2.ext2\n"));
    }

    public static class Base {

        @EnableSetup
        static TestEnvironmentSetup base1 = it -> null;
        @EnableSetup
        static TestEnvironmentSetup base2 = it -> null;
    }

    public static class Ext1 implements TestEnvironmentSetup {
        @Override
        public Object setup(TestExtension extension) {
            return null;
        }
    }

    public static class Ext2 extends Ext1 {}

    @Disabled // prevent direct execution
    @TestGuiceyApp(value = AutoScanApplication.class, setup = {Ext1.class, Ext2.class})
    public static class Test1 extends Base {

        @EnableSetup
        static TestEnvironmentSetup ext1 = it -> null;
        @EnableSetup
        static TestEnvironmentSetup ext2 = it -> null;

        @Test
        void test() {
        }
    }

    @Disabled // prevent direct execution
    public static class Test2 extends Base {

        @RegisterExtension
        static TestGuiceyAppExtension app = TestGuiceyAppExtension.forApp(AutoScanApplication.class)
                .setup(Ext1.class, Ext2.class)
                .setup(it -> null, it -> null)
                .create();

        @EnableSetup
        static TestEnvironmentSetup ext1 = it -> null;
        @EnableSetup
        static TestEnvironmentSetup ext2 = it -> null;

        @Test
        void test() {
        }
    }
}
