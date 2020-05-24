package ru.vyarus.dropwizard.guice.test.jupiter;

import com.ginsberg.junit.exit.ExpectSystemExit;
import com.github.blindpirate.extensions.CaptureSystemOutput;
import com.google.inject.AbstractModule;
import com.google.inject.name.Named;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.support.TestConfiguration;
import ru.vyarus.dropwizard.guice.support.feature.DummyCommand;

import javax.inject.Inject;

/**
 * @author Vyacheslav Rusakov
 * @since 25.05.2020
 */
public class StartErrorJupiterTest {

    @Test
    @ExpectSystemExit
    void checkStartupFail() throws Exception {
        ErrorApplication.main("server");
    }

    @Test
    @ExpectSystemExit
    @CaptureSystemOutput
    // NOTE not parallelizable test!
    void checkStartupFailWithOutput(CaptureSystemOutput.OutputCapture output) throws Exception {
        // assertion declared before
        output.expect(Matchers.containsString(
                "No implementation for java.lang.String annotated with @com.google.inject.name.Named(value=unknown) was bound"));

        ErrorApplication.main("server");
    }

    public static class ErrorApplication extends Application<TestConfiguration> {
        public static void main(String... args) throws Exception {
            new ErrorApplication().run(args);
        }

        @Override
        public void initialize(Bootstrap<TestConfiguration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .modules(new ErrorModule())
                    .build()
            );
            bootstrap.addCommand(new DummyCommand(bootstrap.getApplication()));
        }

        @Override
        public void run(TestConfiguration configuration, Environment environment) throws Exception {
        }
    }

    public static class ErrorModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(ErrorService.class).asEagerSingleton();
        }
    }

    public static class ErrorService {

        @Inject
        ErrorService(@Named("unknown") String unknown) {
        }
    }
}
