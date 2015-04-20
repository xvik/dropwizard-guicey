package ru.vyarus.dropwizard.guice.test

import io.dropwizard.setup.Environment
import org.junit.Rule
import ru.vyarus.dropwizard.guice.injector.lookup.InjectorLookup
import ru.vyarus.dropwizard.guice.support.AutoScanApplication
import ru.vyarus.dropwizard.guice.support.TestConfiguration
import spock.lang.Specification

import java.lang.reflect.Field


/**
 * @author Vyacheslav Rusakov 
 * @since 20.04.2015
 */
class MultipleRulesTest extends Specification {

    @Rule
    GuiceyAppRule<TestConfiguration> RULE = new GuiceyAppRule<>(AutoScanApplication, null);

    @Rule
    GuiceyAppRule<TestConfiguration> RULE2 = new GuiceyAppRule<>(AutoScanApplication, null);

    void setup() {
        // check injectors registered
        Field injectors = InjectorLookup.getDeclaredField("INJECTORS")
        injectors.setAccessible(true)
        assert injectors.get(null).size() == 2
    }

    void cleanupSpec() {
        // check injectors correctly unregistered
        Field injectors = InjectorLookup.getDeclaredField("INJECTORS")
        injectors.setAccessible(true)
        assert injectors.get(null).size() == 0
    }

    def "Check multiple rules"() {

        expect: "guice contexts are different and registered in lookup"
        RULE.getBean(Environment) != RULE2.getBean(Environment)
        InjectorLookup.getInjector(RULE.getApplication()).isPresent()
        InjectorLookup.getInjector(RULE2.getApplication()).isPresent()

    }
}