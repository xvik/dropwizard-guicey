package ru.vyarus.dropwizard.guice.test

import com.google.inject.Key
import io.dropwizard.setup.Environment
import org.junit.Rule
import ru.vyarus.dropwizard.guice.support.AutoScanApplication
import ru.vyarus.dropwizard.guice.support.TestConfiguration
import spock.lang.Specification


/**
 * @author Vyacheslav Rusakov 
 * @since 03.01.2015
 */
class GuiceyRuleTest extends Specification {

    @Rule
    GuiceyAppRule<TestConfiguration> RULE = new GuiceyAppRule<>(AutoScanApplication, null);

    def "Test rule usage"() {

        expect: "app initialized"
        RULE.getInjector().getExistingBinding(Key.get(Environment))
    }
}