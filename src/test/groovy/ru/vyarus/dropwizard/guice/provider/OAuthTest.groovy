package ru.vyarus.dropwizard.guice.provider

import groovyx.net.http.HTTPBuilder
import groovyx.net.http.HttpResponseException
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.support.provider.oauth.OauthCheckApplication
import ru.vyarus.dropwizard.guice.test.spock.UseDropwizardApp

import javax.ws.rs.core.HttpHeaders

/**
 * @author Vyacheslav Rusakov 
 * @since 14.10.2015
 */
@UseDropwizardApp(OauthCheckApplication)
class OAuthTest extends AbstractTest {

    def "Check oath"() {

        when: "calling resource with auth"
        new HTTPBuilder("http://localhost:8080/prototype/").get(
                headers: ["${HttpHeaders.AUTHORIZATION}": "Bearer valid"])

        then: "user authorized"
        true

        when: "calling resource with invalid auth"
        new HTTPBuilder("http://localhost:8080/prototype/").get(
                headers: ["${HttpHeaders.AUTHORIZATION}": "Bearer invalid"])

        then: "user not authorized"
        def ex = thrown(HttpResponseException)
        ex.statusCode == 401
    }
}