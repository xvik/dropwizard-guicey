package ru.vyarus.dropwizard.guice.resource

import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.support.resource.PrototypeResource
import ru.vyarus.dropwizard.guice.support.resource.ResourceSingletonCheckApplication
import ru.vyarus.dropwizard.guice.support.resource.SingletonResource
import ru.vyarus.dropwizard.guice.test.spock.UseDropwizardApp

/**
 * Check resources are singletons
 * @author Vyacheslav Rusakov 
 * @since 04.10.2014
 */
@UseDropwizardApp(ResourceSingletonCheckApplication)
class ResourcesSingletonTest extends AbstractTest {


    def "Check resource singleton"() {

        when: "calling prototype resource"
        new URL("http://localhost:8080/prototype/").getText()
        new URL("http://localhost:8080/prototype/").getText()
        then: "resource instantiated one time, because singletons are forced"
        PrototypeResource.callCounter == 2
        PrototypeResource.creationCounter == 1

        when: "calling singleton resource"
        new URL("http://localhost:8080/singleton/").getText()
        new URL("http://localhost:8080/singleton/").getText()
        then: "resource instantiated one time"
        SingletonResource.callCounter == 2
        SingletonResource.creationCounter == 1
    }
}