package ru.vyarus.dropwizard.guice.config.option

import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.GuiceyOptions
import ru.vyarus.dropwizard.guice.config.option.support.OtherOptions
import ru.vyarus.dropwizard.guice.config.option.support.SampleOptions
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.module.context.option.Options
import ru.vyarus.dropwizard.guice.module.context.option.OptionsInfo
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBootstrap
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle
import ru.vyarus.dropwizard.guice.module.installer.feature.eager.EagerSingleton
import ru.vyarus.dropwizard.guice.test.spock.UseGuiceyApp
import spock.lang.Specification

import javax.inject.Inject


/**
 * @author Vyacheslav Rusakov
 * @since 13.08.2016
 */
@UseGuiceyApp(OptionsApp)
class OptionsAccessTest extends Specification {

    @Inject
    OptionsInfo info
    @Inject
    GuiceyConfigurationInfo ginfo

    def "Check options access"() {

        expect: "all asserts ok"
        true

        and: "info correct"
        info.options.findAll {
            it.declaringClass != GuiceyOptions
        } == [SampleOptions.BoolFalse, SampleOptions.BoolTrue, SampleOptions.NullOption] as Set
        info.optionGroups == [GuiceyOptions, SampleOptions]

        and: "set/read option info correct"
        info.getValue(SampleOptions.NullOption) == "SAMPLE"
        info.isSet(SampleOptions.NullOption)
        info.isUsed(SampleOptions.NullOption)

        and: "default value option read correct"
        info.getValue(SampleOptions.BoolFalse) == false
        !info.isSet(SampleOptions.BoolFalse)
        info.isUsed(SampleOptions.BoolFalse)
    }

    def "Check not used option access"() {

        when: "checking if not used option set"
        info.isSet(OtherOptions.Opt1)
        then: "error"
        thrown(IllegalArgumentException)

        when: "accessing if not used option used"
        info.isUsed(OtherOptions.Opt1)
        then: "error"
        thrown(IllegalArgumentException)

        when: "accessing not used option value"
        info.getValue(OtherOptions.Opt1)
        then: "error"
        thrown(IllegalArgumentException)

    }

    def "GCI contains options info"() {

        expect:
        ginfo.getOptions() == info

    }

    static class OptionsApp extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .option(SampleOptions.NullOption, "SAMPLE")
                    .bundles(
                    new GuiceyBundle() {
                        @Override
                        void initialize(GuiceyBootstrap bs) {
                            assert bs.option(SampleOptions.NullOption) == "SAMPLE"
                            // reading default value
                            assert bs.option(SampleOptions.BoolFalse) == false
                        }
                    })
                    .extensions(Singleton)
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {

        }
    }

    @EagerSingleton
    static class Singleton {

        @Inject
        Singleton(Options options) {
            assert options.get(SampleOptions.NullOption) == "SAMPLE"
            // reading default value
            assert options.get(SampleOptions.BoolTrue) == true
        }
    }
}