package ru.vyarus.dropwizard.guice

import com.google.inject.Stage
import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.module.context.option.OptionsInfo
import ru.vyarus.dropwizard.guice.test.spock.UseGuiceyApp
import spock.lang.Specification

import javax.inject.Inject


/**
 * @author Vyacheslav Rusakov
 * @since 13.08.2016
 */
@UseGuiceyApp(DevStageApp)
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