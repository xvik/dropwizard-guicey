package ru.vyarus.dropwizard.guice.test.jupiter.debug;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import ru.vyarus.dropwizard.guice.AbstractPlatformTest;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook;
import ru.vyarus.dropwizard.guice.support.AutoScanApplication;
import ru.vyarus.dropwizard.guice.test.EnableHook;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;
import ru.vyarus.dropwizard.guice.test.jupiter.env.EnableSetup;
import ru.vyarus.dropwizard.guice.test.jupiter.env.TestEnvironmentSetup;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.TestGuiceyAppExtension;

import static com.google.common.truth.Truth.assertThat;

/**
 * @author Vyacheslav Rusakov
 * @since 29.05.2022
 */
public class HookObjectsLogTest extends AbstractPlatformTest {

    @Test
    void checkSetupOutputForAnnotation() {
        String output = run(Test1.class);
        assertThat(output).contains("Guicey test extensions (Test1.):\n" +
                        "\n" +
                        "\tSetup objects = \n" +
                "\t\t<lambda>                       \t@EnableSetup Test1#setup                           at r.v.d.g.t.j.d.HookObjectsLogTest$Test1#setup\n" +
                "\t\tWebClientFieldsSupport         \tlookup (service loader)                            at r.v.d.g.t.j.ext.(GuiceyExtensionsSupport.java:451)\n" +
                "\t\tWebResourceClientFieldsSupport \tlookup (service loader)                            at r.v.d.g.t.j.ext.(GuiceyExtensionsSupport.java:451)\n" +
                "\t\tLogFieldsSupport               \tlookup (service loader)                            at r.v.d.g.t.j.ext.(GuiceyExtensionsSupport.java:451)\n" +
                "\t\tRestStubFieldsSupport          \tlookup (service loader)                            at r.v.d.g.t.j.ext.(GuiceyExtensionsSupport.java:451)\n" +
                "\t\tStubFieldsSupport              \tlookup (service loader)                            at r.v.d.g.t.j.ext.(GuiceyExtensionsSupport.java:451)\n" +
                "\t\tMockFieldsSupport              \tlookup (service loader)                            at r.v.d.g.t.j.ext.(GuiceyExtensionsSupport.java:451)\n" +
                "\t\tModelFieldsSupport             \tlookup (service loader)                            at r.v.d.g.t.j.ext.(GuiceyExtensionsSupport.java:451)\n" +
                "\t\tSpyFieldsSupport               \tdefault extension                                  at r.v.d.g.t.j.ext.(GuiceyExtensionsSupport.java:455)\n" +
                "\t\tTrackerFieldsSupport           \tdefault extension                                  at r.v.d.g.t.j.ext.(GuiceyExtensionsSupport.java:455)\n" +
                "\n" +
                "\tTest hooks = \n" +
                "\t\t<lambda>                       \t@EnableHook Base#base1                             at r.v.d.g.t.j.d.HookObjectsLogTest$Base#base1\n" +
                "\t\t<lambda>                       \t@EnableHook Base#base2                             at r.v.d.g.t.j.d.HookObjectsLogTest$Base#base2\n" +
                "\t\tExt1                           \t@TestGuiceyApp(hooks)\n" +
                "\t\tExt2                           \t@TestGuiceyApp(hooks)\n" +
                "\t\tExt3                           \t@EnableSetup Test1#setup.hooks(class)              at r.v.d.g.t.j.d.HookObjectsLogTest.(HookObjectsLogTest.java:142)\n" +
                "\t\tExt4                           \t@EnableSetup Test1#setup.hooks(class)              at r.v.d.g.t.j.d.HookObjectsLogTest.(HookObjectsLogTest.java:142)\n" +
                "\t\t<lambda>                       \t@EnableSetup Test1#setup.hooks(obj)                at r.v.d.g.t.j.d.HookObjectsLogTest.(HookObjectsLogTest.java:143)\n" +
                "\t\tExt5                           \t@EnableSetup Test1#setup.hooks(obj)                at r.v.d.g.t.j.d.HookObjectsLogTest.(HookObjectsLogTest.java:143)\n" +
                "\t\t<lambda>                       \t@EnableHook Test1#ext1                             at r.v.d.g.t.j.d.HookObjectsLogTest$Test1#ext1\n" +
                "\t\t<lambda>                       \t@EnableHook Test1#ext2                             at r.v.d.g.t.j.d.HookObjectsLogTest$Test1#ext2");

        assertThat(output).contains(
                "Guicey time after [Before each] of HookObjectsLogTest$Test1#test(): 111 ms \n" +
                        "\n" +
                        "\t[Before all]                       : 111 ms \n" +
                        "\t\tGuicey fields search               : 111 ms \n" +
                        "\t\tGuicey hooks registration          : 111 ms \n" +
                        "\t\tGuicey setup objects execution     : 111 ms \n" +
                        "\t\tDropwizardTestSupport creation     : 111 ms \n" +
                        "\t\tApplication start                  : 111 ms \n" +
                        "\n" +
                        "\t[Before each]                      : 111 ms \n" +
                        "\t\tGuice fields injection             : 111 ms");
    }

    @Test
    void checkSetupOutputForManualRegistration() {
        String output = run(Test2.class);
        assertThat(output).contains("Guicey test extensions (Test2.):\n" +
                        "\n" +
                        "\tSetup objects = \n" +
                "\t\t<lambda>                       \t@EnableSetup Test2#setup                           at r.v.d.g.t.j.d.HookObjectsLogTest$Test2#setup\n" +
                "\t\tWebClientFieldsSupport         \tlookup (service loader)                            at r.v.d.g.t.j.ext.(GuiceyExtensionsSupport.java:451)\n" +
                "\t\tWebResourceClientFieldsSupport \tlookup (service loader)                            at r.v.d.g.t.j.ext.(GuiceyExtensionsSupport.java:451)\n" +
                "\t\tLogFieldsSupport               \tlookup (service loader)                            at r.v.d.g.t.j.ext.(GuiceyExtensionsSupport.java:451)\n" +
                "\t\tRestStubFieldsSupport          \tlookup (service loader)                            at r.v.d.g.t.j.ext.(GuiceyExtensionsSupport.java:451)\n" +
                "\t\tStubFieldsSupport              \tlookup (service loader)                            at r.v.d.g.t.j.ext.(GuiceyExtensionsSupport.java:451)\n" +
                "\t\tMockFieldsSupport              \tlookup (service loader)                            at r.v.d.g.t.j.ext.(GuiceyExtensionsSupport.java:451)\n" +
                "\t\tModelFieldsSupport             \tlookup (service loader)                            at r.v.d.g.t.j.ext.(GuiceyExtensionsSupport.java:451)\n" +
                "\t\tSpyFieldsSupport               \tdefault extension                                  at r.v.d.g.t.j.ext.(GuiceyExtensionsSupport.java:455)\n" +
                "\t\tTrackerFieldsSupport           \tdefault extension                                  at r.v.d.g.t.j.ext.(GuiceyExtensionsSupport.java:455)\n" +
                "\n" +
                "\tTest hooks = \n" +
                "\t\t<lambda>                       \t@EnableHook Base#base1                             at r.v.d.g.t.j.d.HookObjectsLogTest$Base#base1\n" +
                "\t\t<lambda>                       \t@EnableHook Base#base2                             at r.v.d.g.t.j.d.HookObjectsLogTest$Base#base2\n" +
                "\t\tExt1                           \t@RegisterExtension.hooks(class)                    at r.v.d.g.t.j.d.HookObjectsLogTest.(HookObjectsLogTest.java:160)\n" +
                "\t\tExt2                           \t@RegisterExtension.hooks(class)                    at r.v.d.g.t.j.d.HookObjectsLogTest.(HookObjectsLogTest.java:160)\n" +
                "\t\t<lambda>                       \t@RegisterExtension.hooks(obj)                      at r.v.d.g.t.j.d.HookObjectsLogTest.(HookObjectsLogTest.java:161)\n" +
                "\t\t<lambda>                       \t@RegisterExtension.hooks(obj)                      at r.v.d.g.t.j.d.HookObjectsLogTest.(HookObjectsLogTest.java:161)\n" +
                "\t\tExt3                           \t@EnableSetup Test2#setup.hooks(class)              at r.v.d.g.t.j.d.HookObjectsLogTest.(HookObjectsLogTest.java:167)\n" +
                "\t\tExt4                           \t@EnableSetup Test2#setup.hooks(class)              at r.v.d.g.t.j.d.HookObjectsLogTest.(HookObjectsLogTest.java:167)\n" +
                "\t\t<lambda>                       \t@EnableSetup Test2#setup.hooks(obj)                at r.v.d.g.t.j.d.HookObjectsLogTest.(HookObjectsLogTest.java:168)\n" +
                "\t\t<lambda>                       \t@EnableSetup Test2#setup.hooks(obj)                at r.v.d.g.t.j.d.HookObjectsLogTest.(HookObjectsLogTest.java:168)\n" +
                "\t\t<lambda>                       \t@EnableHook Test2#ext1                             at r.v.d.g.t.j.d.HookObjectsLogTest$Test2#ext1\n" +
                "\t\tExt5                           \t@EnableHook Test2#ext2                             at r.v.d.g.t.j.d.HookObjectsLogTest$Test2#ext2\n");

        assertThat(output).contains(
                "Guicey time after [Before each] of HookObjectsLogTest$Test2#test(): 111 ms \n" +
                        "\n" +
                        "\t[Before all]                       : 111 ms \n" +
                        "\t\tGuicey fields search               : 111 ms \n" +
                        "\t\tGuicey hooks registration          : 111 ms \n" +
                        "\t\tGuicey setup objects execution     : 111 ms \n" +
                        "\t\tDropwizardTestSupport creation     : 111 ms \n" +
                        "\t\tApplication start                  : 111 ms \n" +
                        "\n" +
                        "\t[Before each]                      : 111 ms \n" +
                        "\t\tGuice fields injection             : 111 ms");
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

    public static class Ext5 extends Ext1 {}


    @Disabled // prevent direct execution
    @TestGuiceyApp(value = AutoScanApplication.class, hooks = {Ext1.class, Ext2.class}, debug = true)
    public static class Test1 extends Base {

        @EnableSetup
        static TestEnvironmentSetup setup = it -> it
                .hooks(Ext3.class, Ext4.class)
                .hooks(t -> {}, new Ext5());

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
                .debug()
                .create();

        @EnableSetup
        static TestEnvironmentSetup setup = it -> it
                .hooks(Ext3.class, Ext4.class)
                .hooks(t -> {}, t -> {});

        @EnableHook
        static GuiceyConfigurationHook ext1 = it -> {};
        @EnableHook
        static GuiceyConfigurationHook ext2 = new Ext5();

        @Test
        void test() {
        }
    }

    @Override
    protected String clean(String out) {
        return unifyMs(out);
    }
}
