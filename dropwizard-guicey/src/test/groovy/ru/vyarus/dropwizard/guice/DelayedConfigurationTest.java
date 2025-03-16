package ru.vyarus.dropwizard.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import io.dropwizard.lifecycle.Managed;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo;
import ru.vyarus.dropwizard.guice.support.DefaultTestApp;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;

/**
 * @author Vyacheslav Rusakov
 * @since 13.03.2025
 */
public class DelayedConfigurationTest extends AbstractPlatformTest {

    @Test
    void testDelayedConfiguration() {
        String out = runSuccess(Test1.class);
        org.assertj.core.api.Assertions.assertThat(out)
                .contains("Mng                          (r.v.d.g.DelayedConfigurationTest)");
    }

    @Test
    void testDelayedConfigurationDisable() {
        String out = runSuccess(Test2.class);
        org.assertj.core.api.Assertions.assertThat(out)
                .doesNotContain("Mng                          (r.v.d.g.DelayedConfigurationTest)");
    }


    @TestGuiceyApp(Test1.App.class)
    @Disabled
    public static class Test1 {

        @Inject
        GuiceyConfigurationInfo info;

        @Test
        void testDelayedConfig() {
            Assertions.assertTrue(info.getExtensions().contains(Mng.class));
            Assertions.assertTrue(info.getModules().contains(Mod.class));
        }

        public static class App extends DefaultTestApp {
            @Override
            protected GuiceBundle configure() {
                return GuiceBundle.builder()
                        .whenConfigurationReady(environment ->
                                environment
                                        .extensions(Mng.class)
                                        .modules(new Mod()))
                        .build();
            }
        }
    }

    @TestGuiceyApp(Test2.App.class)
    @Disabled
    public static class Test2 {

        @Inject
        GuiceyConfigurationInfo info;

        @Test
        void testDelayedConfig() {
            Assertions.assertFalse(info.getExtensions().contains(Mng.class));
            Assertions.assertTrue(info.getExtensionsDisabled().contains(Mng.class));
            Assertions.assertFalse(info.getModules().contains(Mod.class));
            Assertions.assertTrue(info.getModulesDisabled().contains(Mod.class));
        }

        public static class App extends DefaultTestApp {
            @Override
            protected GuiceBundle configure() {
                return GuiceBundle.builder()
                        .whenConfigurationReady(environment ->
                                environment
                                        .extensions(Mng.class)
                                        .disableExtensions(Mng.class)
                                        .modules(new Mod())
                                        .disableModules(Mod.class))
                        .build();
            }
        }
    }



    public static class Mng implements Managed {}

    public static class Mod extends AbstractModule {}

    @Override
    protected String clean(String out) {
        return out;
    }
}
