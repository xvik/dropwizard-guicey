package ru.vyarus.dropwizard.guice.examples

import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp
import spock.lang.Specification

/**
 * @author Vyacheslav Rusakov
 * @since 22.10.2020
 */
@TestDropwizardApp(GspSpaApplication)
class RouteTest extends Specification {

    def "Check route url leads to html page"() {

        when: "loading index page"
        def index = new URL("http://localhost:8080/app/").getText()
        then: "index loaded"
        index.contains("<html lang=\"en\">")

        when: "loading route"
        def route = new URL("http://localhost:8080/app/foo").getText()
        then: "index loaded"
        route == index
    }

    def "Check app2 routing"() {

        when: "loading index page"
        def index = new URL("http://localhost:8080/app2/").getText()
        then: "index loaded"
        index.contains("<html lang=\"en\">")

        when: "loading route"
        def route = new URL("http://localhost:8080/app2/foo").getText()
        then: "index loaded"
        route == index
    }

    def "Check app3 routing"() {

        when: "loading index page"
        def index = new URL("http://localhost:8080/app3/").getText()
        then: "index loaded"
        index.contains("<html lang=\"en\">")

        when: "loading route"
        def route = new URL("http://localhost:8080/app3/foo").getText()
        then: "index loaded"
        route == index
    }
}
