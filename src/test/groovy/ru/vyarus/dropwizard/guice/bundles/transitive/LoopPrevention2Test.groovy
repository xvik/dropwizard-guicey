package ru.vyarus.dropwizard.guice.bundles.transitive

import org.junit.runners.model.Statement
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.bundles.transitive.support.loop2.LoopApp2
import ru.vyarus.dropwizard.guice.test.GuiceyAppRule

/**
 * @author Vyacheslav Rusakov
 * @since 26.06.2016
 */
class LoopPrevention2Test extends AbstractTest {

    def "Check loop prevention"() {

        when: "starting app"
        new GuiceyAppRule(LoopApp2, null).apply({} as Statement, null).evaluate()
        then: "startup failed"
        def ex = thrown(IllegalStateException)
        ex.getCause().getMessage() == "Bundles registration loop detected: ( LoopBundle1 -> LoopBundle2 ) -> LoopBundle1 ..."
    }
}
