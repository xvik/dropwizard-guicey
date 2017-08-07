package ru.vyarus.dropwizard.guice.provider

import groovyx.net.http.RESTClient
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.support.provider.paramconv.ParamConverterApp
import ru.vyarus.dropwizard.guice.test.spock.UseDropwizardApp

/**
 * @author Vyacheslav Rusakov 
 * @since 03.09.2015
 */
@UseDropwizardApp(ParamConverterApp)
class ParamConverterTest extends AbstractTest {

    def "check param converter registration"() {

        when: "calling resource with custom param"
        def res = new RESTClient("http://localhost:8080/param/valllue").get([:])
        then: "ok"
        res.data.value == 'valllue'
    }

}