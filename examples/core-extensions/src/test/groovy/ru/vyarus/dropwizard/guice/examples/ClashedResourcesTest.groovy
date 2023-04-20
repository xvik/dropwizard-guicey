package ru.vyarus.dropwizard.guice.examples

import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp
import spock.lang.Specification

/**
 * @author Vyacheslav Rusakov
 * @since 30.12.2019
 */
@TestDropwizardApp(ExtensionsClashApplication)
class ClashedResourcesTest extends Specification {

    def "Check resource call"() {

        expect: "call resources"
        new URL("http://localhost:8080/sample").getText() == "foo"
        new URL("http://localhost:8080/sample2").getText() == "foo"
        new URL("http://localhost:8080/sample3").getText() == "foo"
    }
}
