package ru.vyarus.dropwizard.guice.provider

import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.support.provider.exceptionmapper.ExceptionMapperApp2
import ru.vyarus.dropwizard.guice.test.ClientSupport
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp

/**
 * @author Vyacheslav Rusakov 
 * @since 17.04.2015
 */
@TestDropwizardApp(ExceptionMapperApp2)
class HkManagedExceptionMapperTest extends AbstractTest {

    def "Check exception mapper registration through hk2"(ClientSupport client) {

        when: "calling resource which trigger io exception"
        def res = client.targetMain("/ex/").request().get()
        then:
        res.status == 400
        res.readEntity(String) == 'ERROR: IO exception!'
    }

}