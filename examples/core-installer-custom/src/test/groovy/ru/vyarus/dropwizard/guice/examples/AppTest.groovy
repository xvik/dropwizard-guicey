package ru.vyarus.dropwizard.guice.examples

import com.google.inject.Injector
import com.google.inject.Key
import ru.vyarus.dropwizard.guice.examples.installer.MarkersInstaller
import ru.vyarus.dropwizard.guice.examples.service.SampleMarker
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp
import spock.lang.Specification

import jakarta.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 29.01.2016
 */
@TestGuiceyApp(CustomInstallerApplication)
class AppTest extends Specification {

    @Inject
    GuiceyConfigurationInfo info
    @Inject
    Injector injector

    def "Check feature installation"() {

        expect: "installer and feature registered"
        info.installers.contains(MarkersInstaller)
        injector.getExistingBinding(Key.get(SampleMarker)) != null

    }
}