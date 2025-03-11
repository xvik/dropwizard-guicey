package ru.vyarus.dropwizard.guice.test.hook

import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.hook.ConfigurationHooksSupport
import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook
import ru.vyarus.dropwizard.guice.module.context.stat.StatsTracker
import spock.lang.Specification

/**
 * @author Vyacheslav Rusakov
 * @since 13.04.2018
 */
class ConfigurationHooksSupportTest extends Specification {

    void cleanupSpec() {
        // to avoid side effects for other tests
        ConfigurationHooksSupport.reset()
    }

    def "Check hook registration lifecycle"() {

        when: "register hook"
        ({} as GuiceyConfigurationHook).register()
        then: "ok"
        ConfigurationHooksSupport.count() == 1

        when: "check processing"
        ConfigurationHooksSupport.run(GuiceBundle.builder(), new StatsTracker())
        then: "hooks flushed"
        ConfigurationHooksSupport.count() == 0

        when: "check reset call"
        ({} as GuiceyConfigurationHook).register()
        ConfigurationHooksSupport.reset()
        then: "ok"
        ConfigurationHooksSupport.count() == 0
    }

    def "Check listener execution"() {

        def init = false

        when:
        ({ init = true } as GuiceyConfigurationHook).register()
        ConfigurationHooksSupport.run(GuiceBundle.builder(), new StatsTracker())
        then: "called"
        init

    }
}
