package ru.vyarus.dropwizard.guice

import com.google.inject.Stage
import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp
import spock.lang.Specification

import javax.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 13.08.2016
 */
@TestGuiceyApp(DevStageApp)
class DevStageTest extends Specification {

    @Inject
    GuiceyConfigurationInfo info

    def "Check development stage"() {

        expect: "dev stage used"
        info.options.getValue(GuiceyOptions.InjectorStage) == Stage.DEVELOPMENT

    }

    static class DevStageApp extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .build(Stage.DEVELOPMENT))
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {

        }
    }
}