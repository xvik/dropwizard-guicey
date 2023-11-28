package ru.vyarus.dropwizard.guice.test.general;

import io.dropwizard.Application;
import io.dropwizard.setup.Environment;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.support.AutoScanApplication;
import ru.vyarus.dropwizard.guice.support.TestConfiguration;
import ru.vyarus.dropwizard.guice.test.TestSupport;
import ru.vyarus.dropwizard.guice.test.cmd.CommandResult;

/**
 * @author Vyacheslav Rusakov
 * @since 20.11.2023
 */
public class AppStartupFailTest {

    @Test
    void testAppStartupFail() {

        final CommandResult<TestConfiguration> res = TestSupport.buildCommandRunner(App.class).run("server");

        Assertions.assertThat(res.getException().getMessage()).isEqualTo("Something went wrong");
    }

    @Test
    void testStartupPrevention() {
        final CommandResult<TestConfiguration> res = TestSupport.buildCommandRunner(AutoScanApplication.class)
                .runApp();

        Assertions.assertThat(res.getException().getMessage())
                .isEqualTo("Application was expected to fail on startup, but successfully started instead");
    }

    @Test
    void testAppCheck() {
        final CommandResult<TestConfiguration> res = TestSupport.buildCommandRunner(App.class)
                .run("check");

        Assertions.assertThat(res.isSuccess()).isTrue();
        Assertions.assertThat(res.getOutput()).contains("Configuration is OK");
    }

    public static class App extends Application<TestConfiguration> {

        @Override
        public void run(TestConfiguration configuration, Environment environment) throws Exception {
            throw new IllegalStateException("Something went wrong");
        }
    }
}
