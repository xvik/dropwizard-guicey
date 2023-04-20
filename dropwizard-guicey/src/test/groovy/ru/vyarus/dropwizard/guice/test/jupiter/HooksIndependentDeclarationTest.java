package ru.vyarus.dropwizard.guice.test.jupiter;

import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook;
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo;
import ru.vyarus.dropwizard.guice.module.installer.feature.eager.EagerSingleton;

/**
 * @author Vyacheslav Rusakov
 * @since 02.05.2020
 */
public class HooksIndependentDeclarationTest {

    @TestGuiceyApp(value = App.class, hooks = Disable1.class)
    @Nested
    class One {
        @Test
        void checkCorrectDisable(GuiceyConfigurationInfo info) {
            Assertions.assertEquals(1, info.getExtensionsDisabled().size());
            Assertions.assertTrue(info.getExtensionsDisabled().contains(Eager1.class));
        }
    }

    @TestGuiceyApp(value = App.class, hooks = Disable2.class)
    @Nested
    class Two {
        @Test
        void checkCorrectDisable(GuiceyConfigurationInfo info) {
            Assertions.assertEquals(1, info.getExtensionsDisabled().size());
            Assertions.assertTrue(info.getExtensionsDisabled().contains(Eager2.class));
        }
    }


    @EagerSingleton
    public static class Eager1 {
    }

    @EagerSingleton
    public static class Eager2 {
    }

    public static class App extends Application<Configuration> {

        @Override
        public void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .extensions(Eager1.class, Eager2.class)
                    .build());
        }

        @Override
        public void run(Configuration configuration, Environment environment) throws Exception {
        }
    }

    public static class Disable1 implements GuiceyConfigurationHook {
        @Override
        public void configure(GuiceBundle.Builder builder) {
            builder.disableExtensions(Eager1.class);
        }
    }

    public static class Disable2 implements GuiceyConfigurationHook {
        @Override
        public void configure(GuiceBundle.Builder builder) {
            builder.disableExtensions(Eager2.class);
        }
    }
}
