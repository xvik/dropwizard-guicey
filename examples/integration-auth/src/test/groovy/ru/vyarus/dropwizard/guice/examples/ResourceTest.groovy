package ru.vyarus.dropwizard.guice.examples


import ru.vyarus.dropwizard.guice.test.ClientSupport
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp
import spock.lang.Specification

import javax.ws.rs.core.HttpHeaders

/**
 * @author Vyacheslav Rusakov
 * @since 27.01.2016
 */
@TestDropwizardApp(AuthApplication)
class ResourceTest extends Specification {

    def "Check auth"(ClientSupport client) {

        when: "calling resource without auth"
        def res = client.targetApp('/adm').request().get()
        then: "user not authorized"
        res.status == 401


        when: "calling resource without auth"
        res = client.targetApp('/auth').request().get()
        then: "user authorized"
        res.status == 401


        when: "calling resource with incorrect auth"
        res = client.targetApp('/adm').request()
                .header(HttpHeaders.AUTHORIZATION, "Bearer invalid").get()
        then: "user not authorized"
        res.status == 401


        when: "calling resource with proper auth and role"
        res = client.targetApp('/adm').request()
                .header(HttpHeaders.AUTHORIZATION, "Bearer valid").get()
        then: "user authorized"
        res.status == 200
        res.readEntity(String) == 'admin'


        when: "calling resource required auth"
        res = client.targetApp('/auth').request()
                .header(HttpHeaders.AUTHORIZATION, "Bearer valid").get()
        then: "user authorized"
        res.status == 200
        res.readEntity(String) == 'admin'


        when: "calling resource using user without proper role"
        res = client.targetApp('/deny').request()
                .header(HttpHeaders.AUTHORIZATION, "Bearer valid").get()
        then: "user not authorized"
        res.status == 403

    }
}
