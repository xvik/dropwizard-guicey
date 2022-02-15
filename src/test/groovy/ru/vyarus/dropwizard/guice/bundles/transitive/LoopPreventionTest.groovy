package ru.vyarus.dropwizard.guice.bundles.transitive

import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.bundles.transitive.support.loop.LoopApplication
import ru.vyarus.dropwizard.guice.test.TestSupport

/**
 * @author Vyacheslav Rusakov
 * @since 21.06.2016
 */
class LoopPreventionTest extends AbstractTest {

    def "Check bundle loop prevention"() {

        when: "starting app"
        TestSupport.runCoreApp(LoopApplication, null)
        then: "startup failed"
        def ex = thrown(IllegalStateException)
        ex.getMessage() == "Bundles registration loop detected: ( LoopBundle ) -> LoopBundle ..."

    }
}