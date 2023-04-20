package ru.vyarus.dropwizard.examples

import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp
import ru.vyarus.guice.examples.GspApplication
import spock.lang.Specification

/**
 * @author Vyacheslav Rusakov
 * @since 23.10.2019
 */
@TestDropwizardApp(value = GspApplication, restMapping = "/rest/*")
class AppPathsTest extends Specification {

    def "Check application urls"() {

        when: "call css file"
        def res = new URL("http://localhost:8080/style.css").getText()
        then: "correct"
        res.contains("html, body {")

        when: "call direct template"
        res = new URL("http://localhost:8080/foo.ftl").getText()
        then: "ok"
        res.contains("Hello, it's a template:")

        when: "call parametrized view"
        res = new URL("http://localhost:8080/person/12").getText()
        then: "ok"
        res.contains("Hello, John Doe 12!")

        when: "call for index page"
        res = new URL("http://localhost:8080/").getText()
        then: "view rendered"
        res.contains("Hello, John Doe 1!")
    }

}
