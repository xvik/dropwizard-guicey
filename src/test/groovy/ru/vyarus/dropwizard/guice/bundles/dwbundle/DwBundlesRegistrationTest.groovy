package ru.vyarus.dropwizard.guice.bundles.dwbundle

import com.google.inject.Inject
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.test.spock.UseGuiceyApp
import spock.lang.Specification

/**
 * @author Vyacheslav Rusakov 
 * @since 02.08.2015
 */
@UseGuiceyApp(DwBundleApplication)
class DwBundlesRegistrationTest extends Specification {

    @Inject
    GuiceyConfigurationInfo info

    def "Check dw bundle processed"() {

        when: "application started"
        then: "installers registered"
        info.installers.size() == 6

    }

}