package ru.vyarus.dropwizard.guice.test.rest;

import io.dropwizard.core.Application;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import org.junit.jupiter.api.Test;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.test.TestSupport;
import ru.vyarus.dropwizard.guice.test.rest.support.Resource1;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Vyacheslav Rusakov
 * @since 16.10.2025
 */
public class ApplicationRunCompatibilityTest {
    @Test
    void testSimpleRun() throws Exception {
        final RestStubsHook rest = RestStubsHook.builder()
                .disableDropwizardExceptionMappers(true)
                .build();
        TestSupport.build(App.class)
                .hooks(rest)
                .runCore(injector -> {

                    assertThat(injector.getInstance(Environment.class).jersey()
                            .getResourceConfig().isRegistered(Resource1.class)).isTrue();
                    return null;
                });

    }

    public static class App extends Application<Configuration> {

        @Override
        public void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder().build());
        }

        @Override
        public void run(Configuration configuration, Environment environment) throws Exception {
            environment.jersey().register(Resource1.class);
        }
    }
}
