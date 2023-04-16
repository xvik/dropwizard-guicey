package ru.vyarus.dropwizard.guice.examples

import ru.vyarus.dropwizard.guice.examples.web.SampleRequestListener
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp
import spock.lang.Specification

import jakarta.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 31.12.2019
 */
@TestDropwizardApp(ServletsDemoApplication)
class WebFeaturesTest extends Specification {

    @Inject
    SampleRequestListener listener

    def "Check registered servlets and filters execution"() {

        expect: "servlet and filter registered"
        new URL("http://localhost:8080/sample").getText() == "fltr srvlt"

        and: "servlet module servlet and filter registered with guice module"
        new URL("http://localhost:8080/gsample").getText() == "fltr guice srvlt guice"

        and: "guice servlets are visile in admin context"
        new URL("http://localhost:8081/gsample").getText() == "fltr guice srvlt guice"

        and: "admin context servlet registered"
        new URL("http://localhost:8081/admin").getText() == "srvlt admin"

        and: "listener detected 2 calls"
        listener.calls == 2

    }
}
