package ru.vyarus.dropwizard.guice.provider

import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.support.provider.paramconv.Foo
import ru.vyarus.dropwizard.guice.support.provider.paramconv.ParamConverterApp
import ru.vyarus.dropwizard.guice.test.ClientSupport
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp

/**
 * @author Vyacheslav Rusakov 
 * @since 03.09.2015
 */
@TestDropwizardApp(ParamConverterApp)
class ParamConverterTest extends AbstractTest {

    def "check param converter registration"(ClientSupport client) {

        when: "calling resource with custom param"
        def res = client.targetMain("/param/valllue").request().get()
        then: "ok"
        res.readEntity(Foo).value == 'valllue'
    }

}