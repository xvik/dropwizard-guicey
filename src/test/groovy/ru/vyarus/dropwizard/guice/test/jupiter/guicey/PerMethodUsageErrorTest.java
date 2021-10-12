package ru.vyarus.dropwizard.guice.test.jupiter.guicey;

import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.platform.testkit.engine.EngineTestKit;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.TestGuiceyAppExtension;

import javax.inject.Inject;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

/**
 * @author Vyacheslav Rusakov
 * @since 13.10.2021
 */
public class PerMethodUsageErrorTest {

    @Test
    void checkIncorrectUsage() {

        EngineTestKit.engine("junit-jupiter")
                .configurationParameter("junit.jupiter.conditions.deactivate", "org.junit.*DisabledCondition")
                .selectors(selectClass(TestPerMethodRegistration.class))
                .execute()
                .testEvents()
                .debug()
                .assertStatistics(stats -> stats.failed(1));
    }

    @Disabled // prevent direct execution
    public static class TestPerMethodRegistration {

        @RegisterExtension
        TestGuiceyAppExtension extension = TestGuiceyAppExtension.forApp(App.class)
                .create();

        @Inject
        Environment environment;

        @Test
        public void testInject() {
            Assertions.assertNotNull(environment);
        }
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
}
