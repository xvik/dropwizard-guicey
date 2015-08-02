package ru.vyarus.dropwizard.guice.bundles.dwbundle

import com.google.inject.Inject
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.module.installer.internal.FeaturesHolder
import ru.vyarus.dropwizard.guice.test.spock.UseGuiceyApp
import spock.lang.Specification


/**
 * @author Vyacheslav Rusakov 
 * @since 02.08.2015
 */
@UseGuiceyApp(DwBundleApplication)
class DwBundlesRegistrationTest extends AbstractTest {

    @Inject
    FeaturesHolder holder

    def "Check dw bundle processed"() {

        when: "application started"
        then: "installers registered"
        holder.installers.size() == 6

    }

}