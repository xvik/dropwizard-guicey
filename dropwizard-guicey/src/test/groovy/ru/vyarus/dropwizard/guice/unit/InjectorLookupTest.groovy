package ru.vyarus.dropwizard.guice.unit

import com.google.inject.Injector
import io.dropwizard.core.Application
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.injector.lookup.InjectorLookup
import ru.vyarus.dropwizard.guice.module.context.SharedConfigurationState

/**
 * @author Vyacheslav Rusakov 
 * @since 20.04.2015
 */
class InjectorLookupTest extends AbstractTest {

    def "Check lookup"() {

        when: "registering first injector"
        def app = Mock(Application)
        new SharedConfigurationState().assignTo(app)
        InjectorLookup.registerInjector(app, Mock(Injector))
        then:
        InjectorLookup.getInjector(app).isPresent()

        when: "duplicate injection"
        InjectorLookup.registerInjector(app, Mock(Injector))
        then:
        thrown(IllegalStateException)
    }
}