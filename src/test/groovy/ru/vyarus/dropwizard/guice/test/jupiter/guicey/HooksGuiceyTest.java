package ru.vyarus.dropwizard.guice.test.jupiter.guicey;

import io.dropwizard.core.Application;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook;
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo;
import ru.vyarus.dropwizard.guice.module.context.ConfigScope;
import ru.vyarus.dropwizard.guice.module.installer.feature.eager.EagerSingleton;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;

import javax.inject.Inject;

/**
 * @author Vyacheslav Rusakov
 * @since 02.05.2020
 */
@TestGuiceyApp(value = HooksGuiceyTest.App.class, hooks = HooksGuiceyTest.DisableHook.class)
public class HooksGuiceyTest {

    @Inject
    GuiceyConfigurationInfo info;

    @Test
    void checkHookApplied() {
        Assertions.assertTrue(info.getActiveScopes().contains(ConfigScope.Hook.getKey()));
        Assertions.assertTrue(info.getExtensionsDisabled().contains(Eager.class));
    }

    @EagerSingleton
    public static class Eager {
    }

    public static class App extends Application<Configuration> {

        @Override
        public void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .extensions(Eager.class)
                    .build());
        }

        @Override
        public void run(Configuration configuration, Environment environment) throws Exception {
        }
    }

    public static class DisableHook implements GuiceyConfigurationHook {
        @Override
        public void configure(GuiceBundle.Builder builder) {
            builder.disableExtensions(Eager.class);
        }
    }
}
