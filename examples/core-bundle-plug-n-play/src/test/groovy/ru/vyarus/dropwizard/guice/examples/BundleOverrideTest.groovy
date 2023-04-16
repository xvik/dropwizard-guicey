package ru.vyarus.dropwizard.guice.examples

import ru.vyarus.dropwizard.guice.examples.bundle.SampleBundle
import ru.vyarus.dropwizard.guice.examples.bundle.service.SampleService
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.module.context.ConfigScope
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp
import spock.lang.Specification

import jakarta.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 31.12.2019
 */
@TestGuiceyApp(PlugnPlayBundleOverrideApplication)
class BundleOverrideTest extends Specification {

    @Inject
    GuiceyConfigurationInfo info
    @Inject
    SampleService service

    def "Check bundle installed"() {

        expect: "bundle overridden config installed"
        info.getInfos(SampleBundle).size() == 1
        with(info.getInfo(SampleBundle)) {
            getRegistrationAttempts() == 2
            getRegistrationScopeType() == ConfigScope.Application
        }
        service.getConfig() == "changed!"

    }
}
