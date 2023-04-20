package ru.vyarus.dropwizard.guice.test

import com.google.inject.Inject
import ru.vyarus.dropwizard.guice.support.AutoScanApplication
import ru.vyarus.dropwizard.guice.support.TestConfiguration
import ru.vyarus.dropwizard.guice.test.spock.ConfigOverride
import ru.vyarus.dropwizard.guice.test.spock.UseGuiceyApp
import spock.lang.Specification


/**
 * @author Vyacheslav Rusakov 
 * @since 03.01.2015
 */
@UseGuiceyApp(value = AutoScanApplication,
        // NOTE: no config file specified
        configOverride = [
                @ConfigOverride(key = "foo", value = "2"),
                @ConfigOverride(key = "bar", value = "12")
        ])
class EmptyConfigOverrideTest extends Specification {

    @Inject
    TestConfiguration configuration

    def "Check config override"() {

        expect: "config overridden (filled)"
        configuration.foo == 2
        configuration.bar == 12
        configuration.baa == 0
    }
}