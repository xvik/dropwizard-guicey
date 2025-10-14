package ru.vyarus.dropwizard.guice.provider

import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.support.provider.oauth.OauthCheckApplication
import ru.vyarus.dropwizard.guice.test.ClientSupport
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp

import javax.ws.rs.core.HttpHeaders

/**
 * @author Vyacheslav Rusakov 
 * @since 14.10.2015
 */
@TestDropwizardApp(OauthCheckApplication)
class OAuthTest extends AbstractTest {

    def "Check oath"(ClientSupport client) {

        when: "calling resource with auth"
        def res = client.targetApp("prototype/").request()
                .header(HttpHeaders.AUTHORIZATION, "Bearer valid").get()

        then: "user authorized"
        res.status == 200

        when: "calling resource with invalid auth"
        res.close()
        res = client.targetApp("prototype/").request()
                .header(HttpHeaders.AUTHORIZATION, "Bearer invalid").get()

        then: "user not authorized"
        res.status == 401

        cleanup:
        res.close()
    }
}