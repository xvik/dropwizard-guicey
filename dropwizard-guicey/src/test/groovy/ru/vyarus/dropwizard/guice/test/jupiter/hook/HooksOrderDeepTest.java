package ru.vyarus.dropwizard.guice.test.jupiter.hook;

import io.dropwizard.core.Application;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook;
import ru.vyarus.dropwizard.guice.module.lifecycle.GuiceyLifecycleAdapter;
import ru.vyarus.dropwizard.guice.module.lifecycle.event.configuration.ConfigurationHooksProcessedEvent;
import ru.vyarus.dropwizard.guice.test.EnableHook;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author Vyacheslav Rusakov
 * @since 05.02.2022
 */
public class HooksOrderDeepTest {

    public static class Base {
        // hook declared in base class
        @EnableHook
        static GuiceyConfigurationHook hook = new BaseHook();
    }

    public static class Middle extends Base {
        // hook declared in base class
        @EnableHook
        static GuiceyConfigurationHook hook2 = new MiddleHook();
    }

    @TestGuiceyApp(value = App.class, hooks = TestHook.class)
    @Nested
    public class TestOrder extends Middle {

        @Test
        void checkHookRecognized() {
        }
    }

    public static class App extends Application<Configuration> {

        @Override
        public void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .listen(new GuiceyLifecycleAdapter() {
                        @Override
                        protected void configurationHooksProcessed(ConfigurationHooksProcessedEvent event) {
                            Assertions.assertEquals(3, event.getHooks().size());
                            // hooks is a set, but linked implementation used to preserve order
                            Assertions.assertEquals(Arrays.asList(BaseHook.class, MiddleHook.class, TestHook.class),
                                    event.getHooks()
                                            .stream()
                                            .map(GuiceyConfigurationHook::getClass)
                                            .collect(Collectors.toList()));
                        }
                    })
                    .build());
        }

        @Override
        public void run(Configuration configuration, Environment environment) throws Exception {
        }
    }

    public static class BaseHook implements GuiceyConfigurationHook {
        @Override
        public void configure(GuiceBundle.Builder builder) {
        }
    }

    public static class MiddleHook implements GuiceyConfigurationHook {
        @Override
        public void configure(GuiceBundle.Builder builder) {
        }
    }

    public static class TestHook implements GuiceyConfigurationHook {
        @Override
        public void configure(GuiceBundle.Builder builder) {
        }
    }
}
