package ru.vyarus.dropwizard.guice

import com.google.inject.Inject
import ru.vyarus.dropwizard.guice.module.installer.internal.FeaturesHolder
import ru.vyarus.dropwizard.guice.support.CustomInstallerApplication
import ru.vyarus.dropwizard.guice.support.feature.CustomFeature
import ru.vyarus.dropwizard.guice.support.installer.CustomInstaller
import ru.vyarus.dropwizard.guice.test.spock.UseGuiceyApp

/**
 * Custom installer found by classpath scanning and install custom feature
 * @author Vyacheslav Rusakov 
 * @since 04.09.2014
 */
@UseGuiceyApp(CustomInstallerApplication)
class CustomInstallerTest extends AbstractTest {

    @Inject
    FeaturesHolder holder

    def "Check custom installer"() {

        when: "application started"

        then: "installer found and custom feature installed"
        holder.getFeatures(CustomInstaller) == [CustomFeature]
        CustomInstaller.feature
    }
}