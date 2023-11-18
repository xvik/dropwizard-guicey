package ru.vyarus.dropwizard.guice.bundles.transitive

import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.bundles.transitive.support.loop2.LoopApp2
import ru.vyarus.dropwizard.guice.test.TestSupport

/**
 * @author Vyacheslav Rusakov
 * @since 26.06.2016
 */
class LoopPrevention2Test extends AbstractTest {

    def "Check loop prevention"() {

        when: "starting app"
        TestSupport.runCoreApp(LoopApp2)
        then: "startup failed"
        def ex = thrown(IllegalStateException)
        ex.getMessage() == "Bundles registration loop detected: ( LoopBundle1 -> LoopBundle2 ) -> LoopBundle1 ..."
    }
}
