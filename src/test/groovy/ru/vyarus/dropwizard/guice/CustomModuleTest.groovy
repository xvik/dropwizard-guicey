package ru.vyarus.dropwizard.guice

import org.junit.Rule
import ru.vyarus.dropwizard.guice.module.installer.feature.ResourceInstaller
import ru.vyarus.dropwizard.guice.module.installer.internal.FeaturesHolder
import ru.vyarus.dropwizard.guice.support.AutowiredModule
import ru.vyarus.dropwizard.guice.support.CustomModuleApplication
import ru.vyarus.dropwizard.guice.support.TestConfiguration
import ru.vyarus.dropwizard.guice.support.feature.InvisibleResource
import ru.vyarus.dropwizard.guice.test.GuiceyAppRule

/**
 * @author Vyacheslav Rusakov 
 * @since 04.09.2014
 */
class CustomModuleTest extends AbstractTest {

    @Rule
    GuiceyAppRule<TestConfiguration> RULE = new GuiceyAppRule<>(CustomModuleApplication, null);

    def "Check custom module"() {

        expect: "module autowiring done and invisible resource implicitly injected"
        AutowiredModule.instance.environment
        AutowiredModule.instance.bootstrap
        AutowiredModule.instance.configuration
        !RULE.getBean(FeaturesHolder).getFeatures(ResourceInstaller).contains(InvisibleResource)
    }
}