package ru.vyarus.dropwizard.guice.cases.taskreqscope

import groovyx.net.http.HTTPBuilder
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.test.spock.UseDropwizardApp

/**
 * @author Vyacheslav Rusakov 
 * @since 03.09.2015
 */
@UseDropwizardApp(RSAwareTaskApp)
class RSAwareTaskTest extends AbstractTest {

    def "Check task with request scope dependency"() {

        when: "calling task with request scope dependency"
        def res = new HTTPBuilder("http://localhost:8081/tasks/rsaware").post([:])
        then: "ok"
        res.getText() == 'success'

    }
}