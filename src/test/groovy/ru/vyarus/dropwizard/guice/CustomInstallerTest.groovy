package ru.vyarus.dropwizard.guice

import com.google.inject.Inject
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.support.CustomInstallerApplication
import ru.vyarus.dropwizard.guice.support.feature.CustomFeature
import ru.vyarus.dropwizard.guice.support.installer.CustomInstaller
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp

/**
 * Custom installer found by classpath scanning and install custom feature
 * @author Vyacheslav Rusakov 
 * @since 04.09.2014
 */
@TestGuiceyApp(CustomInstallerApplication)
class CustomInstallerTest extends AbstractTest {

    @Inject
    GuiceyConfigurationInfo info

    def "Check custom installer"() {

        when: "application started"

        then: "installer found and custom feature installed"
        info.getExtensions(CustomInstaller) == [CustomFeature]
        CustomInstaller.feature
    }
}