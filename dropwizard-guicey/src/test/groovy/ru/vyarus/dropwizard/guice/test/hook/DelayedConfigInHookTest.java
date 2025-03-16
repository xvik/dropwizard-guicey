package ru.vyarus.dropwizard.guice.test.hook;

import com.google.inject.Inject;
import io.dropwizard.lifecycle.Managed;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook;
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo;
import ru.vyarus.dropwizard.guice.module.context.info.ExtensionItemInfo;
import ru.vyarus.dropwizard.guice.module.context.info.ItemId;
import ru.vyarus.dropwizard.guice.support.DefaultTestApp;
import ru.vyarus.dropwizard.guice.test.EnableHook;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;

/**
 * @author Vyacheslav Rusakov
 * @since 16.03.2025
 */
@TestGuiceyApp(DefaultTestApp.class)
public class DelayedConfigInHookTest {

    @EnableHook
    static GuiceyConfigurationHook hook = builder -> builder
            .extensions(Mng1.class)
            .whenConfigurationReady(environment -> environment.extensions(Mng2.class))
            .printDiagnosticInfo();

    @Inject
    GuiceyConfigurationInfo info;

    @Test
    void testCorrectRegistrationContext() {
        Assertions.assertTrue(info.getExtensions().contains(Mng1.class));
        Assertions.assertTrue(info.getExtensions().contains(Mng2.class));

        ExtensionItemInfo ext = info.getInfo(Mng1.class);
        Assertions.assertEquals(1, ext.getRegisteredBy().size());
        Assertions.assertEquals(ItemId.from(GuiceyConfigurationHook.class), ext.getRegisteredBy().iterator().next());

        ext = info.getInfo(Mng2.class);
        Assertions.assertEquals(1, ext.getRegisteredBy().size());
        Assertions.assertEquals(ItemId.from(GuiceyConfigurationHook.class), ext.getRegisteredBy().iterator().next());
    }


    public static class Mng1 implements Managed {}
    public static class Mng2 implements Managed {}
}
