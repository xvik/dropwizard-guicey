package ru.vyarus.dropwizard.guice

import com.google.inject.Injector
import io.dropwizard.testing.junit.DropwizardAppRule
import org.junit.Rule
import ru.vyarus.dropwizard.guice.module.installer.internal.FeaturesHolder
import ru.vyarus.dropwizard.guice.support.AutowiredModule
import ru.vyarus.dropwizard.guice.support.CustomInstallerApplication
import ru.vyarus.dropwizard.guice.support.CustomModuleApplication
import ru.vyarus.dropwizard.guice.support.TestConfiguration
import ru.vyarus.dropwizard.guice.support.feature.CustomFeature
import ru.vyarus.dropwizard.guice.support.feature.InvisibleResource
import ru.vyarus.dropwizard.guice.support.installer.CustomInstaller
import spock.lang.Specification


/**
 * @author Vyacheslav Rusakov 
 * @since 04.09.2014
 */
class CustomModuleTest extends AbstractTest {

    @Rule
    DropwizardAppRule<TestConfiguration> RULE =
            new DropwizardAppRule<TestConfiguration>(CustomModuleApplication.class, 'src/test/resources/ru/vyarus/dropwizard/guice/config.yml');

    def "Check custom module"() {

        expect: "module autowiring done and invisible resource implicitly injected"
        AutowiredModule.instance.environment
        AutowiredModule.instance.bootstrap
        AutowiredModule.instance.configuration
        AutowiredModule.instance.environment.jersey().resourceConfig.rootResourceClasses.contains(InvisibleResource)
    }
}