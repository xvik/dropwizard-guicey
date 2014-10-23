package ru.vyarus.dropwizard.guice

import org.junit.Rule
import ru.vyarus.dropwizard.guice.module.installer.internal.FeaturesHolder
import ru.vyarus.dropwizard.guice.support.CustomInstallerApplication
import ru.vyarus.dropwizard.guice.support.TestConfiguration
import ru.vyarus.dropwizard.guice.support.feature.CustomFeature
import ru.vyarus.dropwizard.guice.support.installer.CustomInstaller
import ru.vyarus.dropwizard.guice.test.GuiceyAppRule

/**
 * Custom installer found by classpath scanning and install custom feature
 * @author Vyacheslav Rusakov 
 * @since 04.09.2014
 */
class CustomInstallerTest extends AbstractTest {

    @Rule
    GuiceyAppRule<TestConfiguration> RULE = new GuiceyAppRule<>(CustomInstallerApplication, null);

    def "Check custom installer"() {

        when: "application started"
        FeaturesHolder holder = RULE.getBean(FeaturesHolder.class);

        then: "installer found and custom feature installed"
        holder.getFeatures(CustomInstaller) == [CustomFeature]
        CustomInstaller.feature
    }
}