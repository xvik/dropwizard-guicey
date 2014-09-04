package ru.vyarus.dropwizard.guice.unit

import io.dropwizard.Configuration
import io.dropwizard.jersey.setup.JerseyEnvironment
import io.dropwizard.jetty.MutableServletContextHandler
import io.dropwizard.jetty.setup.ServletEnvironment
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.GuiceBundle

/**
 * @author Vyacheslav Rusakov 
 * @since 04.09.2014
 */
class BundleTest extends AbstractTest {

    def "Check injector access"() {

        when: "accessing injector before init"
        GuiceBundle.getInjector()
        then: "fail"
        thrown(NullPointerException)

        when: "accessing injector before init"
        GuiceBundle bundle = GuiceBundle.builder().build()
        bundle.initialize(Mock(Bootstrap))
        bundle.run(Mock(Configuration), mockEnvironment())
        GuiceBundle.getInjector()
        then: "injector available"
        true
    }

    def "Check commands search" () {

        when: "bundle without auto scan but commands search enabled"
        GuiceBundle bundle = GuiceBundle.builder()
                .searchCommands(true)
                .build()
        bundle.initialize(Mock(Bootstrap))
        then:"init failed"
        thrown(IllegalStateException)
    }

}