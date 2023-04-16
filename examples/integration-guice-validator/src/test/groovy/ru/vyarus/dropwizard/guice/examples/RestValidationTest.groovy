package ru.vyarus.dropwizard.guice.examples

import org.glassfish.jersey.client.JerseyClientBuilder
import org.glassfish.jersey.client.JerseyWebTarget
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp
import spock.lang.Specification

import jakarta.ws.rs.BadRequestException
import jakarta.ws.rs.client.Client

/**
 * @author Vyacheslav Rusakov
 * @since 12.01.2018
 */
@TestDropwizardApp(GValApplication)
class RestValidationTest extends Specification {

    def "Check rest methods validation"() {
        setup:
        Client client = JerseyClientBuilder.createClient()
        JerseyWebTarget target = client.target("http://localhost:8080/val/")

        when: "call method with simple validation"
        String res = target.path('q').queryParam('q', 'foo')
                .request().buildGet().invoke(String.class);
        then: "result success"
        res == 'done'

        when: "call method with simple validation without param"
        target.path('q')
                .request().buildGet().invoke(String.class);
        then: "result fail"
        thrown(BadRequestException)


        when: "call method with custom validation"
        res = target.path('custom').queryParam('q', 'foo')
                .request().buildGet().invoke(String.class);
        then: "result success"
        res == 'done'

        when: "call method with custom validation without param"
        target.path('custom').queryParam('q', 'bad')
                .request().buildGet().invoke(String.class);
        then: "result fail, guice enabled validator works"
        thrown(BadRequestException)
    }
}