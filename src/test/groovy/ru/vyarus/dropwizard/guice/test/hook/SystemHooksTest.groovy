package ru.vyarus.dropwizard.guice.test.hook

import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.hook.ConfigurationHooksSupport
import ru.vyarus.dropwizard.guice.hook.GuiceyConfigurationHook
import spock.lang.Specification

/**
 * @author Vyacheslav Rusakov
 * @since 18.08.2019
 */
class SystemHooksTest extends Specification {

    void setup() {
        ConfigurationHooksSupport.reset()
    }

    void cleanup() {
        ConfigurationHooksSupport.reset()
    }

    def "Check system hooks loading"() {

        when: "declare and load hook"
        System.setProperty(ConfigurationHooksSupport.HOOKS_PROPERTY, Hook.name)
        ConfigurationHooksSupport.loadSystemHooks()
        then: "hook loaded"
        ConfigurationHooksSupport.count() == 1

    }

    def "Check alias registration"() {

        when: "register alias"
        ConfigurationHooksSupport.registerSystemHookAlias("hook", Hook)
        then: "alias registered"
        ConfigurationHooksSupport.getSystemHookAliases()["hook"] == Hook.name

        when: "load hook by alias"
        System.setProperty(ConfigurationHooksSupport.HOOKS_PROPERTY, "hook")
        ConfigurationHooksSupport.loadSystemHooks()
        then: "loaded"
        ConfigurationHooksSupport.count() == 1

        when: "declare duplicate alias"
        ConfigurationHooksSupport.registerSystemHookAlias("hook", HookOverride)
        then: "ok, just warning printed"
        true

    }

    static class Hook implements GuiceyConfigurationHook {
        @Override
        void configure(GuiceBundle.Builder builder) {

        }
    }

    static class HookOverride implements GuiceyConfigurationHook {
        @Override
        void configure(GuiceBundle.Builder builder) {

        }
    }
}
