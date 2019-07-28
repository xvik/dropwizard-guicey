package ru.vyarus.dropwizard.guice.config

import io.dropwizard.Application
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.bundle.GuiceyBundleLookup
import ru.vyarus.dropwizard.guice.module.context.ConfigScope
import ru.vyarus.dropwizard.guice.module.installer.scanner.ClasspathScanner
import ru.vyarus.dropwizard.guice.module.jersey.debug.HK2DebugBundle
import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook
import spock.lang.Specification

/**
 * @author Vyacheslav Rusakov
 * @since 16.04.2018
 */
class ScopeRecognitionTest extends Specification {

    def "Check scope recognition"() {

        expect: "recognition"
        ConfigScope.recognize(Application) == ConfigScope.Application
        ConfigScope.recognize(GuiceyBundleLookup) == ConfigScope.BundleLookup
        ConfigScope.recognize(ClasspathScanner) == ConfigScope.ClasspathScan
        ConfigScope.recognize(GuiceyConfigurationHook) == ConfigScope.Hook
        ConfigScope.recognize(HK2DebugBundle) == ConfigScope.GuiceyBundle
        ConfigScope.recognize(GuiceBundle) == ConfigScope.DropwizardBundle

        when: "exemining non bundle class"
        ConfigScope.recognize(Object)
        then: "exception"
        thrown(IllegalStateException)
    }
}
