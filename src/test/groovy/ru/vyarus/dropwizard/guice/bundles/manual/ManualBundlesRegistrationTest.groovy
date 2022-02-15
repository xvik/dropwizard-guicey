package ru.vyarus.dropwizard.guice.bundles.manual

import com.google.inject.Inject
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp

/**
 * @author Vyacheslav Rusakov 
 * @since 02.08.2015
 */
@TestGuiceyApp(ManualBundlesApplication)
class ManualBundlesRegistrationTest extends AbstractTest {

    @Inject
    GuiceyConfigurationInfo info

    def "Check manual bundle processed"() {

        when: "application started"
        then: "installers registered"
        info.installers.size() == 12

    }
}