package ru.vyarus.dropwizard.guice.examples

import ru.vyarus.dropwizard.guice.examples.bundle.SampleBundle
import ru.vyarus.dropwizard.guice.examples.bundle.service.SampleService
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp
import spock.lang.Specification

import jakarta.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 29.01.2016
 */
@TestGuiceyApp(PlugnPlayBundleApplication)
class AppTest extends Specification {

    @Inject
    GuiceyConfigurationInfo info
    @Inject
    SampleService service

    def "Check bundle installed"() {

        expect: "default config installed"
        info.getBundlesFromLookup() as Set == [SampleBundle.class] as Set
        service.getConfig() == "default"

    }
}