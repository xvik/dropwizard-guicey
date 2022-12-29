package ru.vyarus.dropwizard.guice.test.jupiter.dw;

import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook;
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo;
import ru.vyarus.dropwizard.guice.module.context.ConfigScope;
import ru.vyarus.dropwizard.guice.module.installer.feature.eager.EagerSingleton;
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp;
import ru.vyarus.dropwizard.guice.test.jupiter.guicey.HooksGuiceyTest;

import javax.inject.Inject;

/**
 * @author Vyacheslav Rusakov
 * @since 02.05.2020
 */
@TestDropwizardApp(value = HooksDwTest.App.class, hooks = HooksDwTest.DisableHook.class)
public class HooksDwTest {

    @Inject
    GuiceyConfigurationInfo info;

    @Test
    void checkHookApplied() {
        Assertions.assertTrue(info.getActiveScopes().contains(ConfigScope.Hook.getKey()));
        Assertions.assertTrue(info.getExtensionsDisabled().contains(HooksGuiceyTest.Eager.class));
    }

    @EagerSingleton
    public static class Eager {
    }

    public static class App extends Application<Configuration> {

        @Override
        public void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .extensions(HooksGuiceyTest.Eager.class)
                    .build());
        }

        @Override
        public void run(Configuration configuration, Environment environment) throws Exception {
        }
    }

    public static class DisableHook implements GuiceyConfigurationHook {
        @Override
        public void configure(GuiceBundle.Builder builder) {
            builder.disableExtensions(HooksGuiceyTest.Eager.class);
        }
    }
}
