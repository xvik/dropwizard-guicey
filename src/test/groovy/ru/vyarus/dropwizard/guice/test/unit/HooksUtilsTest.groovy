package ru.vyarus.dropwizard.guice.test.unit

import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.hook.ConfigurationHooksSupport
import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook
import ru.vyarus.dropwizard.guice.test.util.HooksUtils
import spock.lang.Specification

/**
 * @author Vyacheslav Rusakov
 * @since 02.05.2020
 */
class HooksUtilsTest extends Specification {

    def "Check hooks initialization"() {
        when: "registering hook"
        def res = HooksUtils.create(TestHook)
        then: "hook registered"
        res.size() == 1
        res[0] instanceof TestHook
    }

    def "Check hooks registration"() {

        when: "registering hook"
        HooksUtils.register([new TestHook()])
        then: "hook registered"
        ConfigurationHooksSupport.count() == 1
    }

    static class TestHook implements GuiceyConfigurationHook {
        @Override
        void configure(GuiceBundle.Builder builder) {

        }
    }
}
