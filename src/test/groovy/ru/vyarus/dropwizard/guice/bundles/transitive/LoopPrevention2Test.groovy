package ru.vyarus.dropwizard.guice.bundles.transitive

import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.bundles.transitive.support.loop2.LoopApp2
import ru.vyarus.dropwizard.guice.bundles.transitive.support.loop2.LoopBundle1
import ru.vyarus.dropwizard.guice.bundles.transitive.support.loop2.LoopBundle2
import ru.vyarus.dropwizard.guice.module.GuiceyConfigurationInfo
import ru.vyarus.dropwizard.guice.test.spock.UseGuiceyApp

import javax.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 26.06.2016
 */
@UseGuiceyApp(LoopApp2)
class LoopPrevention2Test extends AbstractTest {

    @Inject
    GuiceyConfigurationInfo info

    def "Check loop prevention"() {

        expect: "two bundles registered"
        info.bundles.containsAll([LoopBundle1, LoopBundle2])
    }
}
