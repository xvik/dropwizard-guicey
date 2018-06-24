package ru.vyarus.dropwizard.guice.test.hook

import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook
import ru.vyarus.dropwizard.guice.test.spock.ext.GuiceyConfigurationExtension
import spock.lang.Specification

/**
 * @author Vyacheslav Rusakov
 * @since 24.04.2018
 */
class HookInstantiationTest extends Specification {

    def "Check hook instantiation"() {

        when: "ok hook"
        def res = GuiceyConfigurationExtension.instantiate(OKConf)
        then: "ok"
        res.size() == 1

        when: "multiple hooks"
        res = GuiceyConfigurationExtension.instantiate(OKConf, OKConf2)
        then: "ok"
        res.size() == 2
        res[0] instanceof OKConf
        res[1] instanceof OKConf2

        when: "ko hook"
        GuiceyConfigurationExtension.instantiate(KOConf)
        then: "error"
        def ex = thrown(IllegalStateException)
        ex.message.startsWith("Failed to instantiate guicey hook")
    }


    static class OKConf implements GuiceyConfigurationHook {

        @Override
        void configure(GuiceBundle.Builder builder) {

        }
    }

    static class OKConf2 implements GuiceyConfigurationHook {

        @Override
        void configure(GuiceBundle.Builder builder) {

        }
    }

    static class KOConf implements GuiceyConfigurationHook {

        KOConf(boolean oups) {
        }

        @Override
        void configure(GuiceBundle.Builder builder) {

        }
    }
}