package ru.vyarus.dropwizard.guice.unit

import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import ru.vyarus.dropwizard.guice.AbstractTest
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.injector.InjectorFactory;
import com.google.inject.Injector;

/**
 * @author Vyacheslav Rusakov 
 * @since 04.09.2014
 */
class BundleTest extends AbstractTest {

    def "Check injector access"() {

        when: "accessing injector before init"
        GuiceBundle.getInjector()
        then: "fail"
        thrown(NullPointerException)

        when: "accessing injector before init"
        GuiceBundle bundle = GuiceBundle.builder().build()
        bundle.initialize(Mock(Bootstrap))
        bundle.run(Mock(Configuration), mockEnvironment())
        GuiceBundle.getInjector()
        then: "injector available"
        true
    }
    
    def "Check custom injector factory"() {
        InjectorFactory mockInjectorFactory = Mock(InjectorFactory)
        Injector mockInjector = Mock(Injector)
        
        when: "using default factory"
        GuiceBundle bundle = GuiceBundle.builder().build()
        bundle.initialize(Mock(Bootstrap))
        bundle.run(Mock(Configuration), mockEnvironment())        
        then: "injector is a Guice injector"
        GuiceBundle.getInjector() instanceof Injector
        
        when: "using custom factory"        
        bundle = GuiceBundle.builder().injectorFactory(mockInjectorFactory).build()
        bundle.initialize(Mock(Bootstrap))
        bundle.run(Mock(Configuration), mockEnvironment())
        then: "injector factory has been customized"
        1 * mockInjectorFactory.createInjector(*_) >> mockInjector
        GuiceBundle.getInjector() == mockInjector
    }

    def "Check commands search" () {

        when: "bundle without auto scan but commands search enabled"
        GuiceBundle bundle = GuiceBundle.builder()
                .searchCommands(true)
                .build()
        bundle.initialize(Mock(Bootstrap))
        then:"init failed"
        thrown(IllegalStateException)
    }

}