package ru.vyarus.guicey.annotations.lifecycle

import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.module.installer.feature.eager.EagerSingleton
import ru.vyarus.dropwizard.guice.test.TestSupport
import spock.lang.Specification

import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy

/**
 * @author Vyacheslav Rusakov
 * @since 27.11.2018
 */
class MethodFailuresTest extends Specification {

    def "Check PostConstruct failure"() {

        when: 'start method throws exception'
        TestSupport.runCoreApp(FailedApp, null)

        then: 'entire startup fails'
        def ex = thrown(IllegalStateException)
        ex.message.startsWith('Failed to execute method StartFailure.start of instance ru.vyarus.guicey.annotations.lifecycle.MethodFailuresTest$StartFailure')
    }

    def "Check PostStartup failure"() {

        when: 'start server method throws exception'
        TestSupport.runWebApp(ServerFailedApp, null)

        then: 'entire startup fails'
        def ex = thrown(IllegalStateException)
        ex.message.startsWith('Failed to execute method StartupFailure.afterStartup of instance ru.vyarus.guicey.annotations.lifecycle.MethodFailuresTest$StartupFailure')
    }

    def "Check PreDestroy failure"() {

        when: 'destroy method throws exception'
        DestroyFailure bean = TestSupport.runCoreApp(DestroyFailedApp, null) {
            it.getInstance(DestroyFailure)
        }

        then: 'exception suspended'
        bean.called
    }


    def "Check lazy PostConstruct failure"() {

        when: 'start method throws exception, but called after event processing'
        StartFailure bean = TestSupport.runCoreApp(App, null) {
            it.getInstance(StartFailure)
        }

        then: 'method called, error suppressed'
        bean.called
    }

    static class FailedApp extends App {
        FailedApp() {
            super(StartFailure)
        }
    }


    static class ServerFailedApp extends App {
        ServerFailedApp() {
            super(StartupFailure)
        }
    }

    static class DestroyFailedApp extends App {
        DestroyFailedApp() {
            super(DestroyFailure)
        }
    }

    static class App extends Application<Configuration> {

        Class[] exts

        App() {
            this(new Class[0])
        }

        App(Class... exts) {
            this.exts = exts
        }

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap
                    .addBundle(GuiceBundle.builder()
                    // be sure bean initialized with the context and method would be processed when they appear
                            .extensions(exts)
                            .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }

    @EagerSingleton
    static class StartFailure {
        boolean called

        @PostConstruct
        private void start() {
            called = true
            throw new IllegalStateException()
        }
    }

    @EagerSingleton
    static class StartupFailure {

        @PostStartup
        private void afterStartup() {
            throw new IllegalStateException()
        }
    }

    @EagerSingleton
    static class DestroyFailure {
        boolean called

        @PreDestroy
        private void stop() {
            called = true
            throw new IllegalStateException()
        }
    }
}
