package ru.vyarus.dropwizard.guice.test.jupiter.guicey;

import com.google.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook;
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo;
import ru.vyarus.dropwizard.guice.support.AutoScanApplication;
import ru.vyarus.dropwizard.guice.support.TestConfiguration;
import ru.vyarus.dropwizard.guice.support.feature.DummyExceptionMapper;
import ru.vyarus.dropwizard.guice.support.feature.DummyResource;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.TestGuiceyAppExtension;

/**
 * @author Vyacheslav Rusakov
 * @since 03.05.2020
 */
public class ManualRegistrationGuiceyTest {

    @RegisterExtension
    static TestGuiceyAppExtension app = TestGuiceyAppExtension.forApp(AutoScanApplication.class)
            .config("src/test/resources/ru/vyarus/dropwizard/guice/config.yml")
            .configOverrides("foo=2", "bar=12")
            .hooks(Hook.class)
            .hooks(builder -> builder.disableExtensions(DummyResource.class))
            .create();

    @Inject
    TestConfiguration config;

    @Test
    void checkCorrectWiring(GuiceyConfigurationInfo info) {
        Assertions.assertEquals(config.foo, 2);
        Assertions.assertEquals(config.bar, 12);
        Assertions.assertEquals(config.baa, 4);

        Assertions.assertNotNull(info);
        Assertions.assertTrue(info.getExtensionsDisabled().contains(DummyResource.class));
        Assertions.assertTrue(info.getExtensionsDisabled().contains(DummyExceptionMapper.class));
    }

    public static class Hook implements GuiceyConfigurationHook {

        @Override
        public void configure(GuiceBundle.Builder builder) {
            builder.disableExtensions(DummyExceptionMapper.class);
        }
    }
}
