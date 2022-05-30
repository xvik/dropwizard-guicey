package ru.vyarus.dropwizard.guice.test.jupiter.hook;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.platform.testkit.engine.EngineTestKit;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook;
import ru.vyarus.dropwizard.guice.support.AutoScanApplication;
import ru.vyarus.dropwizard.guice.test.EnableHook;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;
import ru.vyarus.dropwizard.guice.test.jupiter.env.EnableSetup;
import ru.vyarus.dropwizard.guice.test.jupiter.env.TestEnvironmentSetup;
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
public class HookObjectsLogTest {

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
                        "\t\tHookObjectsLogTest$Test1$$Lambda$111/1111111 (r.v.d.g.t.j.hook)        \t@EnableSetup field Test1.setup\n" +
                        "\n" +
                        "\tTest hooks = \n" +
                        "\t\tHookObjectsLogTest$Base$$Lambda$111/1111111 (r.v.d.g.t.j.hook)        \t@EnableHook field Base.base1\n" +
                        "\t\tHookObjectsLogTest$Base$$Lambda$111/1111111 (r.v.d.g.t.j.hook)        \t@EnableHook field Base.base2\n" +
                        "\t\tExt1                         (r.v.d.g.t.j.h.HookObjectsLogTest)        \t@TestGuiceyApp\n" +
                        "\t\tExt2                         (r.v.d.g.t.j.h.HookObjectsLogTest)        \t@TestGuiceyApp\n" +
                        "\t\tExt3                         (r.v.d.g.t.j.h.HookObjectsLogTest)        \tHookObjectsLogTest$Test1$$Lambda$111/1111111 class\n" +
                        "\t\tExt4                         (r.v.d.g.t.j.h.HookObjectsLogTest)        \tHookObjectsLogTest$Test1$$Lambda$111/1111111 class\n" +
                        "\t\tHookObjectsLogTest$Test1$$Lambda$111/1111111 (r.v.d.g.t.j.hook)        \tHookObjectsLogTest$Test1$$Lambda$111/1111111 instance\n" +
                        "\t\tHookObjectsLogTest$Test1$$Lambda$111/1111111 (r.v.d.g.t.j.hook)        \tHookObjectsLogTest$Test1$$Lambda$111/1111111 instance\n" +
                        "\t\tHookObjectsLogTest$Test1$$Lambda$111/1111111 (r.v.d.g.t.j.hook)        \t@EnableHook field Test1.ext1\n" +
                        "\t\tHookObjectsLogTest$Test1$$Lambda$111/1111111 (r.v.d.g.t.j.hook)        \t@EnableHook field Test1.ext2\n"));
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
                        "\t\tHookObjectsLogTest$Test2$$Lambda$111/1111111 (r.v.d.g.t.j.hook)        \t@EnableSetup field Test2.setup\n" +
                        "\n" +
                        "\tTest hooks = \n" +
                        "\t\tHookObjectsLogTest$Base$$Lambda$111/1111111 (r.v.d.g.t.j.hook)        \t@EnableHook field Base.base1\n" +
                        "\t\tHookObjectsLogTest$Base$$Lambda$111/1111111 (r.v.d.g.t.j.hook)        \t@EnableHook field Base.base2\n" +
                        "\t\tExt1                         (r.v.d.g.t.j.h.HookObjectsLogTest)        \t@RegisterExtension class\n" +
                        "\t\tExt2                         (r.v.d.g.t.j.h.HookObjectsLogTest)        \t@RegisterExtension class\n" +
                        "\t\tHookObjectsLogTest$Test2$$Lambda$111/1111111 (r.v.d.g.t.j.hook)        \t@RegisterExtension instance\n" +
                        "\t\tHookObjectsLogTest$Test2$$Lambda$111/1111111 (r.v.d.g.t.j.hook)        \t@RegisterExtension instance\n" +
                        "\t\tExt3                         (r.v.d.g.t.j.h.HookObjectsLogTest)        \tHookObjectsLogTest$Test2$$Lambda$111/1111111 class\n" +
                        "\t\tExt4                         (r.v.d.g.t.j.h.HookObjectsLogTest)        \tHookObjectsLogTest$Test2$$Lambda$111/1111111 class\n" +
                        "\t\tHookObjectsLogTest$Test2$$Lambda$111/1111111 (r.v.d.g.t.j.hook)        \tHookObjectsLogTest$Test2$$Lambda$111/1111111 instance\n" +
                        "\t\tHookObjectsLogTest$Test2$$Lambda$111/1111111 (r.v.d.g.t.j.hook)        \tHookObjectsLogTest$Test2$$Lambda$111/1111111 instance\n" +
                        "\t\tHookObjectsLogTest$Test2$$Lambda$111/1111111 (r.v.d.g.t.j.hook)        \t@EnableHook field Test2.ext1\n" +
                        "\t\tHookObjectsLogTest$Test2$$Lambda$111/1111111 (r.v.d.g.t.j.hook)        \t@EnableHook field Test2.ext2\n"));
    }

    public static class Base {

        @EnableHook
        static GuiceyConfigurationHook base1 = it -> {};
        @EnableHook
        static GuiceyConfigurationHook base2 = it -> {};
    }

    public static class Ext1 implements GuiceyConfigurationHook {

        @Override
        public void configure(GuiceBundle.Builder builder) {
        }
    }

    public static class Ext2 extends Ext1 {}

    public static class Ext3 extends Ext1 {}

    public static class Ext4 extends Ext1 {}


    @Disabled // prevent direct execution
    @TestGuiceyApp(value = AutoScanApplication.class, hooks = {Ext1.class, Ext2.class})
    public static class Test1 extends Base {

        @EnableSetup
        static TestEnvironmentSetup setup = it -> it
                .hooks(Ext3.class, Ext4.class)
                .hooks(t -> {}, t -> {});

        @EnableHook
        static GuiceyConfigurationHook ext1 = it -> {};
        @EnableHook
        static GuiceyConfigurationHook ext2 = it -> {};

        @Test
        void test() {
        }
    }

    @Disabled // prevent direct execution
    public static class Test2 extends Base {

        @RegisterExtension
        static TestGuiceyAppExtension app = TestGuiceyAppExtension.forApp(AutoScanApplication.class)
                .hooks(Ext1.class, Ext2.class)
                .hooks(it -> {}, it -> {})
                .create();

        @EnableSetup
        static TestEnvironmentSetup setup = it -> it
                .hooks(Ext3.class, Ext4.class)
                .hooks(t -> {}, t -> {});

        @EnableHook
        static GuiceyConfigurationHook ext1 = it -> {};
        @EnableHook
        static GuiceyConfigurationHook ext2 = it -> {};

        @Test
        void test() {
        }
    }
}
