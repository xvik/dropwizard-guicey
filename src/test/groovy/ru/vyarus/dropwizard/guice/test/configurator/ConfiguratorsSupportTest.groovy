package ru.vyarus.dropwizard.guice.test.configurator

import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.module.support.conf.ConfiguratorsSupport
import ru.vyarus.dropwizard.guice.module.support.conf.GuiceyConfigurator
import spock.lang.Specification

/**
 * @author Vyacheslav Rusakov
 * @since 13.04.2018
 */
class ConfiguratorsSupportTest extends Specification {

    void cleanupSpec() {
        // to avoid side effects for other tests
        ConfiguratorsSupport.reset()
    }

    def "Check configurator registration lifecycle"() {

        when: "register configurator"
        ConfiguratorsSupport.listen({} as GuiceyConfigurator)
        then: "ok"
        ConfiguratorsSupport.count() == 1

        when: "check processing"
        ConfiguratorsSupport.configure(GuiceBundle.builder())
        then: "configurators flushed"
        ConfiguratorsSupport.count() == 0

        when: "check reset call"
        ConfiguratorsSupport.listen({} as GuiceyConfigurator)
        ConfiguratorsSupport.reset()
        then: "ok"
        ConfiguratorsSupport.count() == 0
    }

    def "Check listener execution"() {

        def init = false

        when:
        ConfiguratorsSupport.listen({ init = true} as GuiceyConfigurator)
        ConfiguratorsSupport.configure(GuiceBundle.builder())
        then: "called"
        init

    }
}
