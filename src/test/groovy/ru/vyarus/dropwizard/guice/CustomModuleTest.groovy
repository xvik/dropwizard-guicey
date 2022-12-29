package ru.vyarus.dropwizard.guice

import com.google.inject.Inject
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.module.installer.feature.jersey.ResourceInstaller
import ru.vyarus.dropwizard.guice.support.AutowiredModule
import ru.vyarus.dropwizard.guice.support.CustomModuleApplication
import ru.vyarus.dropwizard.guice.support.feature.InvisibleResource
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp

/**
 * @author Vyacheslav Rusakov 
 * @since 04.09.2014
 */
@TestGuiceyApp(CustomModuleApplication)
class CustomModuleTest extends AbstractTest {

    @Inject
    GuiceyConfigurationInfo info

    def "Check custom module"() {

        expect: "module autowiring done and invisible resource implicitly injected"
        AutowiredModule.instance.environment
        AutowiredModule.instance.bootstrap
        AutowiredModule.instance.configuration
        AutowiredModule.instance.configurationTree
        // direct guice binding
        !info.getExtensions(ResourceInstaller).contains(InvisibleResource)
    }
}