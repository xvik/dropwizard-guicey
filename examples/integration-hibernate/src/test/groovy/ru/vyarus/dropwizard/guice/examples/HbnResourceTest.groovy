package ru.vyarus.dropwizard.guice.examples

import org.glassfish.jersey.client.JerseyClientBuilder
import org.glassfish.jersey.client.JerseyInvocation
import ru.vyarus.dropwizard.guice.examples.model.Sample
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp
import spock.lang.Specification

import javax.ws.rs.client.Client
import javax.ws.rs.core.GenericType

/**
 * @author Vyacheslav Rusakov
 * @since 12.06.2016
 */
@TestDropwizardApp(value = HbnApplication, config = 'src/test/resources/config.yml')
class HbnResourceTest extends Specification {

    def "Check resource call"() {

        setup:
        Client client = JerseyClientBuilder.createClient()
        JerseyInvocation get = client.target("http://localhost:8080/sample").request().buildGet()

        when: "call hbn resource"
        List<Sample> res = get.invoke(new GenericType<List<Sample>>() {
        })
        then: "result success"
        res.size() == 1
        res.get(0).name == 'sample'

        when: "call hbn resource one more time"
        res = get.invoke(new GenericType<List<Sample>>() {
        })
        then: "+1 result successfully returned"
        res.size() == 2
        res.get(1).name == 'sample'
    }
}