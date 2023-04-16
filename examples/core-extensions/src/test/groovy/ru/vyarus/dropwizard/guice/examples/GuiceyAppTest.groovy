package ru.vyarus.dropwizard.guice.examples

import ru.vyarus.dropwizard.guice.examples.service.SampleService
import ru.vyarus.dropwizard.guice.test.jupiter.TestGuiceyApp
import spock.lang.Specification

import jakarta.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 27.01.2016
 */
@TestGuiceyApp(ExtensionsDemoApplication)
class GuiceyAppTest extends Specification {

    @Inject
    SampleService service

    def "Check guice service"() {

        expect: "service injected"
        service.foo() == "foo"
    }
}