package ru.vyarus.dropwizard.guice.test.jupiter.hook;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook;
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo;
import ru.vyarus.dropwizard.guice.support.feature.DummyResource;

/**
 * @author Vyacheslav Rusakov
 * @since 08.05.2020
 */
public class HookAfterTest extends HookAfterBase {

    static GuiceyConfigurationHook hook = builder -> builder.disableExtensions(DummyResource.class);

    @Test
    void checkHookRecognized(GuiceyConfigurationInfo info) {
        Assertions.assertTrue(info.getExtensionsDisabled().contains(DummyResource.class));
    }
}
