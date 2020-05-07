package ru.vyarus.dropwizard.guice.test.jupiter.hook;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook;
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo;
import ru.vyarus.dropwizard.guice.support.AutoScanApplication;
import ru.vyarus.dropwizard.guice.support.feature.DummyResource;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;

/**
 * @author Vyacheslav Rusakov
 * @since 08.05.2020
 */

public class HookBeforeTest {

    public static class Base {
        // hook declared in base class
        static GuiceyConfigurationHook hook = builder -> builder.disableExtensions(DummyResource.class);
    }

    @TestGuiceyApp(AutoScanApplication.class)
    @Nested
    public class TestBefore extends Base {

        @Test
        void checkHookRecognized(GuiceyConfigurationInfo info) {
            Assertions.assertTrue(info.getExtensionsDisabled().contains(DummyResource.class));
        }
    }
}
