package ru.vyarus.dropwizard.guice.test

import com.google.inject.Injector
import io.dropwizard.setup.Environment
import org.junit.Rule
import ru.vyarus.dropwizard.guice.injector.lookup.InjectorLookup
import ru.vyarus.dropwizard.guice.module.context.SharedConfigurationState
import ru.vyarus.dropwizard.guice.support.AutoScanApplication
import ru.vyarus.dropwizard.guice.support.TestConfiguration
import spock.lang.Specification

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
        assert SharedConfigurationState.statesCount() == 2
        def inj1 = SharedConfigurationState.lookup(RULE.getApplication(), Injector).get()
        def inj2 = SharedConfigurationState.lookup(RULE2.getApplication(), Injector).get()
        assert inj1 != inj2
    }

    void cleanupSpec() {
        // check injectors correctly unregistered
        assert SharedConfigurationState.statesCount() == 0
    }

    def "Check multiple rules"() {

        expect: "guice contexts are different and registered in lookup"
        RULE.getBean(Environment) != RULE2.getBean(Environment)
        InjectorLookup.getInjector(RULE.getApplication()).isPresent()
        InjectorLookup.getInjector(RULE2.getApplication()).isPresent()

    }
}