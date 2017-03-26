package ru.vyarus.dropwizard.guice.test

import org.junit.Rule
import org.junit.contrib.java.lang.system.internal.CheckExitCalled
import spock.lang.Specification


/**
 * @author Vyacheslav Rusakov
 * @since 25.03.2017
 */
class StartupErrorRuleTest extends Specification {

    @Rule
    StartupErrorRule rule = StartupErrorRule.pending()

    def "Check exit catch"() {

        rule.arm()

        when: "exiting"
        System.out.println 'sample out'
        System.err.println 'sample err'
        System.exit(1)

        then: "exit intercepted"
        thrown(CheckExitCalled)
        rule.output == 'sample out'
        rule.error == 'sample err'
    }
}