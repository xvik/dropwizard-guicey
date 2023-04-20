package ru.vyarus.dropwizard.guice.test.jupiter.setup;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook;
import ru.vyarus.dropwizard.guice.support.AutoScanApplication;
import ru.vyarus.dropwizard.guice.test.EnableHook;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;
import ru.vyarus.dropwizard.guice.test.jupiter.env.EnableSetup;
import ru.vyarus.dropwizard.guice.test.jupiter.env.TestEnvironmentSetup;
import ru.vyarus.dropwizard.guice.test.jupiter.env.TestExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Vyacheslav Rusakov
 * @since 18.05.2022
 */
@TestGuiceyApp(AutoScanApplication.class)
public class SetupAndHookInOneFieldTest {

    @EnableHook
    @EnableSetup
    static Hook hook = new Hook();

    @Test
    void testBothHooksApplied() {
        Assertions.assertEquals(Arrays.asList("setup", "hook"), hook.actions);
    }

    public static class Hook implements TestEnvironmentSetup, GuiceyConfigurationHook {

        public List<String> actions = new ArrayList<>();

        @Override
        public void configure(GuiceBundle.Builder builder) {
            actions.add("hook");
        }

        @Override
        public Object setup(TestExtension extension) {
            return actions.add("setup");
        }
    }
}
