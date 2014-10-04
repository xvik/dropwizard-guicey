package ru.vyarus.dropwizard.guice.request

import io.dropwizard.testing.junit.DropwizardAppRule
import org.junit.Rule
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.support.TestConfiguration
import ru.vyarus.dropwizard.guice.support.request.RequestBeansApplication
import ru.vyarus.dropwizard.guice.support.request.RequestScopedBean

/**
 * Check request scoped beans supported.
 *
 * @author Vyacheslav Rusakov 
 * @since 04.10.2014
 */
class RequestBeansTest extends AbstractTest {
    @Rule
    DropwizardAppRule<TestConfiguration> RULE =
            new DropwizardAppRule<TestConfiguration>(RequestBeansApplication.class, 'src/test/resources/ru/vyarus/dropwizard/guice/config.yml');

    def "Check request bean"() {

        when: "application started"
        new URL("http://localhost:8080/dummy/").getText()
        then: "rest method internally obtains 3 request scoped beans"
        RequestScopedBean.called
    }

    def "Check task with request bean"() {

        when: "application started"
        new URL("http://localhost:8081/tasks/rs").getText()
        then: "failed because guice filter registered only for jersey context"
        thrown(IOException)
    }
}