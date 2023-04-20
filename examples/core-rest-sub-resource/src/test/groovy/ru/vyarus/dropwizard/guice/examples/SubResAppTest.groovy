package ru.vyarus.dropwizard.guice.examples

import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp
import spock.lang.Specification

/**
 * @author Vyacheslav Rusakov
 * @since 27.07.2017
 */
@TestDropwizardApp(SubResourceApplication)
class SubResAppTest extends Specification {

    def "Check guice sub resource"() {

        when: "call guice managed sub resource"
        def res = new URL("http://localhost:8080/root/12/guice-sub").getText()
        then: "correct"
        res == 'guice root1 12'
    }

    def "Check hk sub resource"() {

        when: "call hk managed sub resource"
        def res = new URL("http://localhost:8080/root/11/hk-sub").getText()
        then: "correct"
        res == 'hk root2 11'
    }
}