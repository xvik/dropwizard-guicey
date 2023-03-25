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
        config = 'src/test/resources/ru/vyarus/dropwizard/guice/config.yml',
        configOverride = [
                @ConfigOverride(key = "foo", value = "2"),
                @ConfigOverride(key = "bar", value = "12")
        ])
class GuiceyConfigOverrideTest extends Specification {

        @Inject
        TestConfiguration configuration

        def "Check config override"() {

                expect: "config overridden"
                configuration.foo == 2
                configuration.bar == 12
                configuration.baa == 4
        }
}