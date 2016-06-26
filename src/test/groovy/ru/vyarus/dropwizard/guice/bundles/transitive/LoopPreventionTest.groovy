package ru.vyarus.dropwizard.guice.bundles.transitive

import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.bundles.transitive.support.loop.LoopApplication
import ru.vyarus.dropwizard.guice.bundles.transitive.support.loop.LoopBundle
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.test.spock.UseGuiceyApp

import javax.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 21.06.2016
 */
@UseGuiceyApp(LoopApplication)
class LoopPreventionTest extends AbstractTest {

    @Inject
    GuiceyConfigurationInfo info

    def "Check bundle loop prevention"() {

        expect:
        info.bundles.contains(LoopBundle)

    }
}