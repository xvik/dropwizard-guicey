package ru.vyarus.dropwizard.guice.config

import com.google.inject.Injector
import io.dropwizard.Configuration
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.config.support.BasicApplication
import ru.vyarus.dropwizard.guice.test.spock.UseGuiceyApp

import javax.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 19.06.2016
 */
@UseGuiceyApp(BasicApplication)
class BasicConfigurationMappingTest extends AbstractTest {

    @Inject
    Injector injector

    def "Check basic configuration mapping"() {

        expect: "configuration bound"
        injector.getBinding(Configuration)
    }
}