package ru.vyarus.dropwizard.guice.examples

import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp
import spock.lang.Specification

/**
 * @author Vyacheslav Rusakov
 * @since 07.03.2017
 */
@TestDropwizardApp(EventBusApp)
class AppTest extends Specification {

    def "Check event bus"() {

        when: "calling foo"
        def res = new URL("http://localhost:8080/sample/foo").getText()
        then: "ok"
        res == "1"

        when: "calling bar"
        res = new URL("http://localhost:8080/sample/bar").getText()
        then: "ok"
        res == "1"

        when: "check stats"
        def stats = new URL("http://localhost:8080/sample/stats").getText()
        then: "ok"
        stats == 'Foo: 1, Bar: 1, Base: 2'
    }
}