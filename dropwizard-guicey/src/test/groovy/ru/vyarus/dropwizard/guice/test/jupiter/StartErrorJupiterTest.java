package ru.vyarus.dropwizard.guice.test.jupiter;

import com.google.inject.AbstractModule;
import com.google.inject.name.Named;
import io.dropwizard.core.Application;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import javax.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.support.TestConfiguration;
import ru.vyarus.dropwizard.guice.support.feature.DummyCommand;
import ru.vyarus.dropwizard.guice.test.builder.TestSupportHolder;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;
import uk.org.webcompere.systemstubs.security.SystemExit;
import uk.org.webcompere.systemstubs.stream.SystemErr;

/**
 * @author Vyacheslav Rusakov
 * @since 25.05.2020
 */
@ExtendWith(SystemStubsExtension.class)
public class StartErrorJupiterTest {

    @SystemStub
    SystemExit exit;
    @SystemStub
    SystemErr err;

    @Test
        // NOTE could be parallelized with some tests, but not with other error tests
    void checkStartupFail() throws Exception {
        exit.execute(() -> {
            ErrorApplication.main("server");
        });
        Assertions.assertEquals(1, exit.getExitCode());
        Assertions.assertFalse(TestSupportHolder.isContextSet());
    }

    @Test
        // NOTE not parallelizable test!
    void checkStartupFailWithOutput() throws Exception {
        exit.execute(() -> {
            ErrorApplication.main("server");
        });
        // strange matching because in java9 @Named value will be quoted and in 8 will not
        Assertions.assertTrue(err.getText()
                .contains("[Guice/MissingImplementation]: No implementation for String annotated with @Named"));
        Assertions.assertFalse(TestSupportHolder.isContextSet());

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
