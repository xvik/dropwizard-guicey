package ru.vyarus.dropwizard.guice.unit

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.inject.Injector
import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.injector.InjectorFactory

/**
 * @author Vyacheslav Rusakov 
 * @since 04.09.2014
 */
class BundleTest extends AbstractTest {

    def "Check injector access"() {

        when: "accessing injector before init"
        GuiceBundle bundle = GuiceBundle.builder().build()
        bundle.getInjector()
        then: "fail"
        thrown(IllegalStateException)

        when: "accessing injector after init"
        bundle.initialize(mockBootstrap())
        bundle.run(new Configuration(), mockEnvironment())
        bundle.getInjector()
        then: "injector available"
        true
    }

    def "Check custom injector factory"() {
        InjectorFactory mockInjectorFactory = Mock(InjectorFactory)
        Injector mockInjector = Mock(Injector)

        when: "using default factory"
        GuiceBundle bundle = GuiceBundle.builder().build()
        bundle.initialize(mockBootstrap())
        bundle.run(new Configuration(), mockEnvironment())
        then: "injector is a Guice injector"
        bundle.getInjector() instanceof Injector

        when: "using custom factory"
        bundle = GuiceBundle.builder().injectorFactory(mockInjectorFactory).build()
        bundle.initialize(mockBootstrap())
        bundle.run(new Configuration(), mockEnvironment())
        then: "injector factory has been customized"
        1 * mockInjectorFactory.createInjector(*_) >> mockInjector
        bundle.getInjector() == mockInjector
    }

    def "Check commands search"() {

        when: "bundle without auto scan but commands search enabled"
        GuiceBundle bundle = GuiceBundle.builder()
                .searchCommands()
                .build()
        bundle.initialize(mockBootstrap())
        then: "init failed"
        thrown(IllegalStateException)
    }

    Bootstrap mockBootstrap(){
        def bootstrap = Mock(Bootstrap)
        bootstrap.application >> Mock(Application)
        bootstrap.objectMapper >> new ObjectMapper()
        return bootstrap
    }
}