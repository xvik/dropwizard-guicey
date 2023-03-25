package ru.vyarus.guicey.jdbi3

import io.dropwizard.core.Application
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import org.jdbi.v3.core.array.SqlArrayArgumentStrategy
import org.jdbi.v3.core.array.SqlArrayTypes
import org.jdbi.v3.core.h2.H2DatabasePlugin
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp
import ru.vyarus.guicey.jdbi3.support.SampleApp
import ru.vyarus.guicey.jdbi3.support.SampleConfiguration

import javax.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 14.09.2018
 */
@TestGuiceyApp(value = App, config = 'src/test/resources/test-config.yml')
class AdvancedConfigurationTest extends AbstractTest {

    @Inject
    Bootstrap bootstrap

    def "Check custom configuration"() {

        expect:
        (bootstrap.getApplication() as App).configCalled
    }

    static class App extends Application<SampleConfiguration> {

        boolean configCalled = false

        @Override
        void initialize(Bootstrap<SampleConfiguration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .enableAutoConfig(SampleApp.package.name)
                    .bundles(JdbiBundle.<SampleConfiguration> forDatabase { conf, env -> conf.database }
                            .withPlugins(new H2DatabasePlugin())
                            .withConfig({ jdbi ->
                                // using block for plugin validation
                                assert jdbi.getConfig(SqlArrayTypes).getArgumentStrategy() == SqlArrayArgumentStrategy.OBJECT_ARRAY
                                configCalled = true
                            }))
                    .build())
        }

        @Override
        void run(SampleConfiguration configuration, Environment environment) throws Exception {
        }
    }
}
