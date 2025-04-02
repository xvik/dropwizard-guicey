package ru.vyarus.dropwizard.guice.config.sharedstate

import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.jetty.MutableServletContextHandler
import io.dropwizard.lifecycle.setup.LifecycleEnvironment
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.module.context.SharedConfigurationState
import spock.lang.Specification

/**
 * @author Vyacheslav Rusakov
 * @since 27.09.2019
 */
class SharedStateTest extends Specification {

    def "Check static state access"() {

        setup:
        Application app = new App()
        SharedConfigurationState state = new SharedConfigurationState()

        when: "access context for not registered app"
        def res = SharedConfigurationState.get(app)
        then: "not found"
        !res.isPresent()

        when: "get or fail"
        SharedConfigurationState.getOrFail(app, "failed %s", 1)
        then: "error"
        def ex = thrown(IllegalStateException)
        ex.message == "failed 1"

        when: "lookup value"
        res = SharedConfigurationState.lookup(app, App)
        then: "null"
        !res.isPresent()

        when: "lookup or fail"
        SharedConfigurationState.lookupOrFail(app, App, "failed %s", 2)
        then: "error"
        ex = thrown(IllegalStateException)
        ex.message == "failed 2"

        when: "access by instance"
        res = state.get(App)
        then: "null"
        res == null

        when: "access and fail"
        state.getOrFail(App, "failed %s", 3)
        then: "error"
        ex = thrown(IllegalStateException)
        ex.message == "failed 3"

        when: "assign value"
        res = state.get(App, { app })
        then: "initial value set"
        res == app

        when: "get with another default"
        res = state.get(App, { new App() })
        then: "same value returned"
        res == app
        state.get(App) == app

        when: "assigned to app"
        state.assignTo app
        then: "static lookup work"
        SharedConfigurationState.get(app).get() == state
        SharedConfigurationState.getOrFail(app, "1") == state
        SharedConfigurationState.lookup(app, App).get() == app
        SharedConfigurationState.lookupOrFail(app, App, "2") == app

        when: "to string state"
        res = state.toString()
        then: "ok"
        res == "Shared state with 1 objects: $App.name"

    }

    def "Check edge cases"() {
        setup:
        Application app = new App()
        SharedConfigurationState state = new SharedConfigurationState()

        when: "put with null key"
        state.put(null, app)
        then: "err"
        def ex = thrown(IllegalArgumentException)
        ex.message == 'Shared state key can\'t be null'

        when: "put with null value"
        state.put(App, null)
        then: "err"
        def ex2 = thrown(IllegalArgumentException)
        ex2.message == 'Shared state does not accept null values'

        when: "get with null supplier"
        def res = state.get(App, null)
        then: "behave as usual get"
        thrown(NullPointerException)

        when: "duplicate assign"
        state.assignTo app
        state.assignTo app
        then: "err"
        def ex3 = thrown(IllegalStateException)
        ex3.message == "Shared state already associated with application $App.name"
    }

    def "Check access by environment"() {
        setup:
        Application app = new App()
        SharedConfigurationState state = new SharedConfigurationState()

        def props = [:]
        def environment = Mock(Environment)
        environment.getApplicationContext() >> Mock(MutableServletContextHandler)
        environment.getApplicationContext().setAttribute(*_) >> { props.put(it[0], it[1]) }
        environment.getApplicationContext().getAttribute(*_) >> { props.get(it[0]) }
        environment.lifecycle() >> Mock(LifecycleEnvironment)


        when: "access context for not registered app"
        def res = SharedConfigurationState.get(environment)
        then: "not found"
        !res.isPresent()

        when: "get or fail"
        SharedConfigurationState.getOrFail(environment, "failed %s", 1)
        then: "error"
        def ex = thrown(IllegalStateException)
        ex.message == "failed 1"

        when: "lookup value"
        res = SharedConfigurationState.lookup(environment, App)
        then: "null"
        !res.isPresent()

        when: "lookup or fail"
        SharedConfigurationState.lookupOrFail(environment, App, "failed %s", 2)
        then: "error"
        ex = thrown(IllegalStateException)
        ex.message == "failed 2"

        when: "assigned to app"
        state.put(App, app)
        state.assignTo app
        state.listen(environment)
        then: "static lookup work"
        props.size() == 1
        props.get(SharedConfigurationState.CONTEXT_APPLICATION_PROPERTY) == app
        SharedConfigurationState.get(environment).get() == state
        SharedConfigurationState.getOrFail(environment, "1") == state
        SharedConfigurationState.lookup(environment, App).get() == app
        SharedConfigurationState.lookupOrFail(environment, App, "2") == app

        when: "to string state"
        res = state.toString()
        then: "ok"
        res == "Shared state with 1 objects: $App.name"
    }

    static class App extends Application<Configuration> {
        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder().build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }
}
