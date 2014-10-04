package ru.vyarus.dropwizard.guice.resource

import io.dropwizard.testing.junit.DropwizardAppRule
import org.junit.Rule
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.support.TestConfiguration
import ru.vyarus.dropwizard.guice.support.resource.PrototypeResource
import ru.vyarus.dropwizard.guice.support.resource.ResourceSingletonCheckApplication
import ru.vyarus.dropwizard.guice.support.resource.SingletonResource

/**
 * Check resources are singletons
 * @author Vyacheslav Rusakov 
 * @since 04.10.2014
 */
class ResourcesSingletonTest extends AbstractTest {

    @Rule
    DropwizardAppRule<TestConfiguration> RULE =
            new DropwizardAppRule<TestConfiguration>(ResourceSingletonCheckApplication.class, 'src/test/resources/ru/vyarus/dropwizard/guice/config.yml');

    def "Check resource singleton"() {

        when: "calling prototype resource"
        new URL("http://localhost:8080/prototype/").getText()
        new URL("http://localhost:8080/prototype/").getText()
        then: "resource instantiated three times (by installer and each request)"
        PrototypeResource.callCounter == 2
        PrototypeResource.creationCounter == 3

        when: "calling singleton resource"
        new URL("http://localhost:8080/singleton/").getText()
        new URL("http://localhost:8080/singleton/").getText()
        then: "resource instantiated one time"
        SingletonResource.callCounter == 2
        SingletonResource.creationCounter == 1
    }
}