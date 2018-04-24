package ru.vyarus.dropwizard.guice.test.configurator

import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.configurator.GuiceyConfigurator
import ru.vyarus.dropwizard.guice.test.spock.ext.GuiceyConfiguratorExtension
import spock.lang.Specification

/**
 * @author Vyacheslav Rusakov
 * @since 24.04.2018
 */
class ConfiguratorInstantiationTest extends Specification {

    def "Check configurator instantiation"() {

        when: "ok configurator"
        def res = GuiceyConfiguratorExtension.instantiate(OKConf)
        then: "ok"
        res.size() == 1

        when: "multiple configurators"
        res = GuiceyConfiguratorExtension.instantiate(OKConf, OKConf2)
        then: "ok"
        res.size() == 2
        res[0] instanceof OKConf
        res[1] instanceof OKConf2

        when: "ko configurator"
        GuiceyConfiguratorExtension.instantiate(KOConf)
        then: "error"
        def ex = thrown(IllegalStateException)
        ex.message.startsWith("Failed to instantiate guicey configurator")
    }


    static class OKConf implements GuiceyConfigurator {

        @Override
        void configure(GuiceBundle.Builder builder) {

        }
    }

    static class OKConf2 implements GuiceyConfigurator {

        @Override
        void configure(GuiceBundle.Builder builder) {

        }
    }

    static class KOConf implements GuiceyConfigurator {

        KOConf(boolean oups) {
        }

        @Override
        void configure(GuiceBundle.Builder builder) {

        }
    }
}