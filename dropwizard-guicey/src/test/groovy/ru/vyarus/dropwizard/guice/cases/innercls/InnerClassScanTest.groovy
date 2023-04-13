package ru.vyarus.dropwizard.guice.cases.innercls

import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.provider.JerseyProviderInstaller
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp
import spock.lang.Specification

import jakarta.inject.Inject

/**
 * @author Vyacheslav Rusakov 
 * @since 12.10.2015
 */
@TestGuiceyApp(InnerClassScanApp)
class InnerClassScanTest extends Specification {

    @Inject
    GuiceyConfigurationInfo info;

    def "Check inner classes scan"() {

        expect:
        info.getExtensions(JerseyProviderInstaller) as Set ==
                [AbstractExceptionMapper.FooExceptionMapper, AbstractExceptionMapper.BarExceptionMapper] as Set

    }
}