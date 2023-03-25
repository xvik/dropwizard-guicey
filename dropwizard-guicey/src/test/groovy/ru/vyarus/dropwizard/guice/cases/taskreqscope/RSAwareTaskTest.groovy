package ru.vyarus.dropwizard.guice.cases.taskreqscope

import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.test.ClientSupport
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp

/**
 * @author Vyacheslav Rusakov 
 * @since 03.09.2015
 */
@TestDropwizardApp(RSAwareTaskApp)
class RSAwareTaskTest extends AbstractTest {

    def "Check task with request scope dependency"(ClientSupport client) {

        when: "calling task with request scope dependency"
        def res = client.targetAdmin("/tasks/rsaware").request().post(null)
        then: "ok"
        res.readEntity(String) == 'success'

    }
}