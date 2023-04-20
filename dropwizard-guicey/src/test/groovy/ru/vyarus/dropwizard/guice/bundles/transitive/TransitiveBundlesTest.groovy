package ru.vyarus.dropwizard.guice.bundles.transitive

import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.bundles.transitive.support.Bundle1
import ru.vyarus.dropwizard.guice.bundles.transitive.support.Bundle2
import ru.vyarus.dropwizard.guice.bundles.transitive.support.Bundle3
import ru.vyarus.dropwizard.guice.bundles.transitive.support.TransitiveBundlesApp
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp

import javax.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 21.06.2016
 */
@TestGuiceyApp(TransitiveBundlesApp)
class TransitiveBundlesTest extends AbstractTest {

    @Inject
    GuiceyConfigurationInfo info

    def "Check transitive bundles installation"() {

        expect: 'transitive bundles registered'
        Bundle1.called
        Bundle2.called
        Bundle3.called
        info.guiceyBundles.containsAll([Bundle1, Bundle2, Bundle3])

    }
}