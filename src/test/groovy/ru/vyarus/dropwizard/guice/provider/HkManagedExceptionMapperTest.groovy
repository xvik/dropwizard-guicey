package ru.vyarus.dropwizard.guice.provider

import groovyx.net.http.HttpResponseException
import groovyx.net.http.RESTClient
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.support.provider.exceptionmapper.ExceptionMapperApp2
import ru.vyarus.dropwizard.guice.test.spock.UseDropwizardApp

/**
 * @author Vyacheslav Rusakov 
 * @since 17.04.2015
 */
@UseDropwizardApp(ExceptionMapperApp2)
class HkManagedExceptionMapperTest extends AbstractTest {

    def "Check exception mapper registration through hk"() {

        when: "calling resource which trigger io exception"
        new RESTClient("http://localhost:8080/ex/").get([:])
        then:
        def ex = thrown(HttpResponseException)
        ex.response.status == 400
        ex.response.data.text == 'ERROR: IO exception!'
    }

}