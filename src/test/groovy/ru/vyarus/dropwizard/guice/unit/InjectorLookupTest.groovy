package ru.vyarus.dropwizard.guice.unit

import com.google.inject.Injector
import io.dropwizard.Application
import io.dropwizard.setup.Bootstrap
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.injector.lookup.InjectorLookup

/**
 * @author Vyacheslav Rusakov 
 * @since 20.04.2015
 */
class InjectorLookupTest extends AbstractTest {

    def "Check lookup"() {

        when: "registering first injector"
        def app = Mock(Application)
        def managed = InjectorLookup.registerInjector(app, Mock(Injector))
        then:
        InjectorLookup.getInjector(app).isPresent()

        when: "duplicate injection"
        InjectorLookup.registerInjector(app, Mock(Injector))
        then:
        thrown(IllegalArgumentException)

        when: "removing injector"
        managed.stop()
        then:
        !InjectorLookup.getInjector(app).isPresent()
    }

    Bootstrap mockBootstrap() {
        def bootstrap = Mock(Bootstrap)
        bootstrap.application >> Mock(Application)
        return bootstrap
    }
}