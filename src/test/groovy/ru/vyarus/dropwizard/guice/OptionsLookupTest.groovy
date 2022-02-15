package ru.vyarus.dropwizard.guice

import com.google.common.collect.ImmutableMap
import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp

import javax.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 21.08.2016
 */
@TestGuiceyApp(OLApp)
class OptionsLookupTest extends AbstractTest {

    @Inject
    GuiceyConfigurationInfo info

    def "Test options multi config"() {

        expect: "options set"
        !info.options.getValue(GuiceyOptions.UseCoreInstallers)
    }

    static class OLApp extends Application<Configuration> {
        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .options(ImmutableMap.<Enum, Object> builder()
                            .put(GuiceyOptions.UseCoreInstallers, false)
                            .build())
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {

        }
    }
}