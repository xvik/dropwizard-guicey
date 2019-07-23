package ru.vyarus.dropwizard.guice.bundles.transitive

import org.junit.runners.model.Statement
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.bundles.transitive.support.loop.LoopApplication
import ru.vyarus.dropwizard.guice.test.GuiceyAppRule

/**
 * @author Vyacheslav Rusakov
 * @since 21.06.2016
 */
class LoopPreventionTest extends AbstractTest {

    def "Check bundle loop prevention"() {

        when: "starting app"
        new GuiceyAppRule(LoopApplication, null).apply({} as Statement, null).evaluate()
        then: "startup failed"
        def ex = thrown(IllegalStateException)
        ex.getCause().getMessage() == "Bundles registration loop detected: ( LoopBundle ) -> LoopBundle ..."

    }
}