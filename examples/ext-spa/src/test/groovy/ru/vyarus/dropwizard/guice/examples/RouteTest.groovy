package ru.vyarus.dropwizard.guice.examples

import ru.vyarus.dropwizard.guice.test.ClientSupport
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp
import spock.lang.Specification

/**
 * @author Vyacheslav Rusakov
 * @since 23.10.2019
 */
@TestDropwizardApp(SpaApplication)
class RouteTest extends Specification {

    def "Check route url leads to html page"(ClientSupport client) {

        when: "loading index page"
        def index = client.targetMain('app/').request().get(String)
        then: "index loaded"
        index.contains("<html lang=\"en\">")

        when: "loading route"
        def route = client.targetMain('app/foo').request().accept('text/html').get(String)
        then: "index loaded"
        route == index
    }
}
