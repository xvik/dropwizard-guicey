package ru.vyarus.dropwizard.guice.provider

import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.support.provider.exceptionmapper.ExceptionMapperApp
import ru.vyarus.dropwizard.guice.test.ClientSupport
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp

/**
 * @author Vyacheslav Rusakov 
 * @since 17.04.2015
 */
@TestDropwizardApp(ExceptionMapperApp)
class ExceptionMapperTest extends AbstractTest {

    def "Check exception mapper registration"(ClientSupport client) {

        when: "calling resource which trigger io exception"
        def res = client.targetMain("/ex/").request().get()
        then:
        res.status == 400
        res.readEntity(String.class) == 'ERROR: IO exception!'

        cleanup:
        res.close()
    }

}