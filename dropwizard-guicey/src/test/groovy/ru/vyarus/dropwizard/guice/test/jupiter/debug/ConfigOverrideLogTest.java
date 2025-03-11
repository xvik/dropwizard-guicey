package ru.vyarus.dropwizard.guice.test.jupiter.debug;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import ru.vyarus.dropwizard.guice.AbstractPlatformTest;
import ru.vyarus.dropwizard.guice.support.AutoScanApplication;
import ru.vyarus.dropwizard.guice.support.TestConfiguration;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;
import ru.vyarus.dropwizard.guice.test.jupiter.env.EnableSetup;
import ru.vyarus.dropwizard.guice.test.jupiter.env.TestEnvironmentSetup;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.TestGuiceyAppExtension;
import ru.vyarus.dropwizard.guice.test.util.ConfigModifier;

import static com.google.common.truth.Truth.assertThat;

/**
 * @author Vyacheslav Rusakov
 * @since 25.06.2022
 */
public class ConfigOverrideLogTest extends AbstractPlatformTest {

    @Test
    void checkSetupOutputForAnnotation() {
        String output = run(Test1.class);

        assertThat(output).contains("Configuration overrides (Test1.):\n" +
                "\t                  bar = 11\n" +
                "\t                  foo = 1\n" +
                "\n" +
                "Configuration modifiers:\n" +
                "\t\tCfgModify1                     \t@TestGuiceyApp(configModifiers)\n" +
                "\t\t<lambda>                       \t@EnableSetup Test1#setup.configModifiers(obj)      at r.v.d.g.t.j.d.ConfigOverrideLogTest.(ConfigOverrideLogTest.java:85)\n" +
                "\t\tCfgModify2                     \t@EnableSetup Test1#setup.configModifiers(class)    at r.v.d.g.t.j.d.ConfigOverrideLogTest.(ConfigOverrideLogTest.java:86)");

        assertThat(output).contains(
                "Guicey time after [Before each] of ConfigOverrideLogTest$Test1#test(): 111 ms\n" +
                        "\n" +
                        "\t[Before all]                       : 111 ms\n" +
                        "\t\tGuicey fields search               : 111 ms\n" +
                        "\t\tGuicey hooks registration          : 111 ms\n" +
                        "\t\tGuicey setup objects execution     : 111 ms\n" +
                        "\t\tDropwizardTestSupport creation     : 111 ms\n" +
                        "\t\tApplication start                  : 111 ms\n" +
                        "\n" +
                        "\t[Before each]                      : 111 ms\n" +
                        "\t\tGuice fields injection             : 111 ms\n");
    }

    @Test
    void checkSetupOutputForManualRegistration() {
        String output = run(Test2.class);

        assertThat(output).contains("Configuration overrides (Test2.):\n" +
                "\t                  foo = 2\n" +
                "\t                  bar = 11\n" +
                "\n" +
                "Configuration modifiers:\n" +
                "\t\t<lambda>                       \t@RegisterExtension.configModifiers(obj)            at r.v.d.g.t.j.d.ConfigOverrideLogTest.(ConfigOverrideLogTest.java:100)\n" +
                "\t\tCfgModify1                     \t@RegisterExtension.configModifiers(class)          at r.v.d.g.t.j.d.ConfigOverrideLogTest.(ConfigOverrideLogTest.java:101)\n" +
                "\t\t<lambda>                       \t@EnableSetup Test2#setup.configModifiers(obj)      at r.v.d.g.t.j.d.ConfigOverrideLogTest.(ConfigOverrideLogTest.java:107)\n" +
                "\t\tCfgModify2                     \t@EnableSetup Test2#setup.configModifiers(class)    at r.v.d.g.t.j.d.ConfigOverrideLogTest.(ConfigOverrideLogTest.java:108)");

        assertThat(output).contains(
                "Guicey time after [Before each] of ConfigOverrideLogTest$Test2#test(): 111 ms\n" +
                        "\n" +
                        "\t[Before all]                       : 111 ms\n" +
                        "\t\tGuicey fields search               : 111 ms\n" +
                        "\t\tGuicey hooks registration          : 111 ms\n" +
                        "\t\tGuicey setup objects execution     : 111 ms\n" +
                        "\t\tDropwizardTestSupport creation     : 111 ms\n" +
                        "\t\tApplication start                  : 111 ms\n" +
                        "\n" +
                        "\t[Before each]                      : 111 ms\n" +
                        "\t\tGuice fields injection             : 111 ms\n");
    }

    @Disabled // prevent direct execution
    @TestGuiceyApp(value = AutoScanApplication.class, configOverride = "foo: 1",
            configModifiers = CfgModify1.class, debug = true)
    public static class Test1 {

        @EnableSetup
        static TestEnvironmentSetup setup = ext ->
                ext.configModifiers(config -> {})
                        .configModifiers(CfgModify2.class)
                        .configOverride("bar", "11");

        @Test
        void test() {
        }
    }

    @Disabled // prevent direct execution
    public static class Test2 {

        @RegisterExtension
        static TestGuiceyAppExtension app = TestGuiceyAppExtension.forApp(AutoScanApplication.class)
                .configOverrides("foo: 2")
                .configModifiers(config -> {})
                .configModifiers(CfgModify1.class)
                .debug()
                .create();

        @EnableSetup
        static TestEnvironmentSetup setup = ext -> ext
                .configModifiers(config -> {})
                .configModifiers(CfgModify2.class)
                .configOverride("bar", "11");

        @Test
        void test() {
        }

    }

    public static class CfgModify1 implements ConfigModifier<TestConfiguration> {
        @Override
        public void modify(TestConfiguration config) throws Exception {
        }
    }

    public static class CfgModify2 extends CfgModify1 {}

    @Override
    protected String clean(String out) {
        return out.replaceAll("\\d+(\\.\\d+)? ms", "111 ms");
    }
}
