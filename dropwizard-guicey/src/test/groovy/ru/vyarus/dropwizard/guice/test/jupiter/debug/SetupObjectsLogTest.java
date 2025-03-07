package ru.vyarus.dropwizard.guice.test.jupiter.debug;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import ru.vyarus.dropwizard.guice.AbstractPlatformTest;
import ru.vyarus.dropwizard.guice.support.AutoScanApplication;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;
import ru.vyarus.dropwizard.guice.test.jupiter.env.EnableSetup;
import ru.vyarus.dropwizard.guice.test.jupiter.env.TestEnvironmentSetup;
import ru.vyarus.dropwizard.guice.test.jupiter.env.TestExtension;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.TestGuiceyAppExtension;

import static com.google.common.truth.Truth.assertThat;

/**
 * @author Vyacheslav Rusakov
 * @since 29.05.2022
 */
public class SetupObjectsLogTest extends AbstractPlatformTest {

    @Test
    void checkSetupOutputForAnnotation() {
        String output = run(Test1.class);
        assertThat(output).contains("Guicey test extensions (Test1.):\n" +
                        "\n" +
                        "\tSetup objects = \n" +
                "\t\tExt1                           \t@TestGuiceyApp(setup)\n" +
                "\t\tExt2                           \t@TestGuiceyApp(setup)\n" +
                "\t\t<lambda>                       \t@EnableSetup Base#base1                            at r.v.d.g.t.j.d.SetupObjectsLogTest$Base#base1\n" +
                "\t\t<lambda>                       \t@EnableSetup Base#base2                            at r.v.d.g.t.j.d.SetupObjectsLogTest$Base#base2\n" +
                "\t\t<lambda>                       \t@EnableSetup Test1#ext1                            at r.v.d.g.t.j.d.SetupObjectsLogTest$Test1#ext1\n" +
                "\t\tExt3                           \t@EnableSetup Test1#ext2                            at r.v.d.g.t.j.d.SetupObjectsLogTest$Test1#ext2\n" +
                "\t\tRecordedLogsSupport            \tlookup (service loader)                            at r.v.d.g.t.j.ext.(GuiceyExtensionsSupport.java:428)\n" +
                "\t\tRestStubSupport                \tlookup (service loader)                            at r.v.d.g.t.j.ext.(GuiceyExtensionsSupport.java:428)\n" +
                "\t\tStubsSupport                   \tlookup (service loader)                            at r.v.d.g.t.j.ext.(GuiceyExtensionsSupport.java:428)\n" +
                "\t\tMocksSupport                   \tlookup (service loader)                            at r.v.d.g.t.j.ext.(GuiceyExtensionsSupport.java:428)\n" +
                "\t\tSpiesSupport                   \tdefault extension                                  at r.v.d.g.t.j.ext.(GuiceyExtensionsSupport.java:432)\n" +
                "\t\tTrackersSupport                \tdefault extension                                  at r.v.d.g.t.j.ext.(GuiceyExtensionsSupport.java:432)\n" +
                "\n" +
                "\tTest hooks = \n" +
                "\t\tRecordedLogsSupport            \tauto recognition                                   at r.v.d.g.test.util.(TestSetupUtils.java:108)\n" +
                "\t\tRestStubSupport                \tauto recognition                                   at r.v.d.g.test.util.(TestSetupUtils.java:108)\n" +
                "\t\tStubsSupport                   \tauto recognition                                   at r.v.d.g.test.util.(TestSetupUtils.java:108)\n" +
                "\t\tMocksSupport                   \tauto recognition                                   at r.v.d.g.test.util.(TestSetupUtils.java:108)\n" +
                "\t\tSpiesSupport                   \tauto recognition                                   at r.v.d.g.test.util.(TestSetupUtils.java:108)\n" +
                "\t\tTrackersSupport                \tauto recognition                                   at r.v.d.g.test.util.(TestSetupUtils.java:108)\n");
    }

    @Test
    void checkSetupOutputForManualRegistration() {
        String output = run(Test2.class);
        assertThat(output).contains("Guicey test extensions (Test2.):\n" +
                        "\n" +
                        "\tSetup objects = \n" +
                "\t\tExt1                           \t@RegisterExtension.setup(class)                    at r.v.d.g.t.j.d.SetupObjectsLogTest.(SetupObjectsLogTest.java:118)\n" +
                "\t\tExt2                           \t@RegisterExtension.setup(class)                    at r.v.d.g.t.j.d.SetupObjectsLogTest.(SetupObjectsLogTest.java:118)\n" +
                "\t\t<lambda>                       \t@RegisterExtension.setup(obj)                      at r.v.d.g.t.j.d.SetupObjectsLogTest.(SetupObjectsLogTest.java:119)\n" +
                "\t\tExt3                           \t@RegisterExtension.setup(obj)                      at r.v.d.g.t.j.d.SetupObjectsLogTest.(SetupObjectsLogTest.java:119)\n" +
                "\t\t<lambda>                       \t@EnableSetup Base#base1                            at r.v.d.g.t.j.d.SetupObjectsLogTest$Base#base1\n" +
                "\t\t<lambda>                       \t@EnableSetup Base#base2                            at r.v.d.g.t.j.d.SetupObjectsLogTest$Base#base2\n" +
                "\t\t<lambda>                       \t@EnableSetup Test2#ext1                            at r.v.d.g.t.j.d.SetupObjectsLogTest$Test2#ext1\n" +
                "\t\t<lambda>                       \t@EnableSetup Test2#ext2                            at r.v.d.g.t.j.d.SetupObjectsLogTest$Test2#ext2\n" +
                "\t\tRecordedLogsSupport            \tlookup (service loader)                            at r.v.d.g.t.j.ext.(GuiceyExtensionsSupport.java:428)\n" +
                "\t\tRestStubSupport                \tlookup (service loader)                            at r.v.d.g.t.j.ext.(GuiceyExtensionsSupport.java:428)\n" +
                "\t\tStubsSupport                   \tlookup (service loader)                            at r.v.d.g.t.j.ext.(GuiceyExtensionsSupport.java:428)\n" +
                "\t\tMocksSupport                   \tlookup (service loader)                            at r.v.d.g.t.j.ext.(GuiceyExtensionsSupport.java:428)\n" +
                "\t\tSpiesSupport                   \tdefault extension                                  at r.v.d.g.t.j.ext.(GuiceyExtensionsSupport.java:432)\n" +
                "\t\tTrackersSupport                \tdefault extension                                  at r.v.d.g.t.j.ext.(GuiceyExtensionsSupport.java:432)\n" +
                "\n" +
                "\tTest hooks = \n" +
                "\t\tRecordedLogsSupport            \tauto recognition                                   at r.v.d.g.test.util.(TestSetupUtils.java:108)\n" +
                "\t\tRestStubSupport                \tauto recognition                                   at r.v.d.g.test.util.(TestSetupUtils.java:108)\n" +
                "\t\tStubsSupport                   \tauto recognition                                   at r.v.d.g.test.util.(TestSetupUtils.java:108)\n" +
                "\t\tMocksSupport                   \tauto recognition                                   at r.v.d.g.test.util.(TestSetupUtils.java:108)\n" +
                "\t\tSpiesSupport                   \tauto recognition                                   at r.v.d.g.test.util.(TestSetupUtils.java:108)\n" +
                "\t\tTrackersSupport                \tauto recognition                                   at r.v.d.g.test.util.(TestSetupUtils.java:108)\n");
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

    public static class Ext3 extends Ext1 {}

    @Disabled // prevent direct execution
    @TestGuiceyApp(value = AutoScanApplication.class, setup = {Ext1.class, Ext2.class}, debug = true)
    public static class Test1 extends Base {

        @EnableSetup
        static TestEnvironmentSetup ext1 = it -> null;
        @EnableSetup
        static TestEnvironmentSetup ext2 = new Ext3();

        @Test
        void test() {
        }
    }

    @Disabled // prevent direct execution
    public static class Test2 extends Base {

        @RegisterExtension
        static TestGuiceyAppExtension app = TestGuiceyAppExtension.forApp(AutoScanApplication.class)
                .setup(Ext1.class, Ext2.class)
                .setup(it -> null, new Ext3())
                .debug()
                .create();

        @EnableSetup
        static TestEnvironmentSetup ext1 = it -> null;
        @EnableSetup
        static TestEnvironmentSetup ext2 = it -> null;

        @Test
        void test() {
        }
    }

    @Override
    protected String clean(String out) {
        return out.replaceAll("\\d+\\.\\d+ ms", "111 ms");
    }
}
