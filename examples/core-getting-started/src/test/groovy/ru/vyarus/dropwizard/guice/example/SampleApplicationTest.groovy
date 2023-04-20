package ru.vyarus.dropwizard.guice.example

import ru.vyarus.dropwizard.guice.examples.SampleApplication
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp
import spock.lang.Specification

/**
 * @author Vyacheslav Rusakov
 * @since 06.03.2017
 */
@TestDropwizardApp(SampleApplication)
class SampleApplicationTest extends Specification {

    def "Check application startup"() {

        when: "call resource directly"
        new URL("http://localhost:8080/sample/").getText()
        then: "not allowed"
        def ex = thrown(IOException)
        ex.message.startsWith('Server returned HTTP response code: 401 for URL')

        when: "call resource correctly"
        def res = new URL("http://localhost:8080/sample/?user=me").getText()
        then: "allowed"
        res
    }
}
