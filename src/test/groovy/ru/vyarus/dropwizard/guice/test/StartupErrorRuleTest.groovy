package ru.vyarus.dropwizard.guice.test

import org.junit.Rule
import spock.lang.Specification

/**
 * @author Vyacheslav Rusakov
 * @since 25.03.2017
 */
class StartupErrorRuleTest extends Specification {

    @Rule
    StartupErrorRule rule = StartupErrorRule.create()

    def "Check exit catch"() {

        when: "exiting"
        System.out.println 'sample out'
        System.err.println 'sample err'
        System.exit(1)

        then: "exit intercepted"
        thrown(rule.indicatorExceptionType)
        rule.output == 'sample out'
        rule.error == 'sample err'
    }

    def "Check empty output"() {

        when: "exiting"
        System.exit(1)

        then: "exit intercepted"
        thrown(rule.indicatorExceptionType)
        rule.output == ''
        rule.error == ''
    }
}