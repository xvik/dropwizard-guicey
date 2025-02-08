package ru.vyarus.dropwizard.guice.test.jupiter.setup;

import io.dropwizard.core.Application;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook;
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp;
import ru.vyarus.dropwizard.guice.test.jupiter.env.TestEnvironmentSetup;
import ru.vyarus.dropwizard.guice.test.jupiter.env.TestExecutionListener;
import ru.vyarus.dropwizard.guice.test.jupiter.env.TestExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Vyacheslav Rusakov
 * @since 08.02.2025
 */
@TestGuiceyApp(value = SetupObjectAutoRegistrationTest.App.class,
        setup = SetupObjectAutoRegistrationTest.SetupObject.class,
        debug = true)
public class SetupObjectAutoRegistrationTest {

    static List<String> actions = new ArrayList<>();

    @Test
    void testDuplicateRegistration() {

        Assertions.assertEquals(Arrays.asList("setup", "configure", "beforeEach"), actions);
    }

    public static class App extends Application<Configuration> {
        @Override
        public void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder().build());
        }

        @Override
        public void run(Configuration configuration, Environment environment) throws Exception {
        }
    }

    public static class SetupObject implements TestEnvironmentSetup, GuiceyConfigurationHook, TestExecutionListener {

        @Override
        public Object setup(TestExtension extension) {
            actions.add("setup");
            // not required manual registration
            return extension.hooks(this).listen(this);
        }


        @Override
        public void configure(GuiceBundle.Builder builder) {
            actions.add("configure");
        }

        @Override
        public void beforeEach(ExtensionContext context) {
            actions.add("beforeEach");
        }
    }
}
