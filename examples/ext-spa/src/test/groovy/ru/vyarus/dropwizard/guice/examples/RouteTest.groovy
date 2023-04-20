package ru.vyarus.dropwizard.guice.examples

import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp
import spock.lang.Specification

/**
 * @author Vyacheslav Rusakov
 * @since 23.10.2019
 */
@TestDropwizardApp(SpaApplication)
class RouteTest extends Specification {

    def "Check rout usl leads to html page"() {

        when: "loading index page"
        def index = new URL("http://localhost:8080/app/").getText()
        then: "index loaded"
        index.contains("<html lang=\"en\">")

        when: "loading route"
        def route = new URL("http://localhost:8080/app/foo").getText()
        then: "index loaded"
        route == index
    }
}
