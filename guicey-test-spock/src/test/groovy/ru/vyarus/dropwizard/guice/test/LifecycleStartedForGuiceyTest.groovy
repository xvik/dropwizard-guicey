package ru.vyarus.dropwizard.guice.test

import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import org.eclipse.jetty.util.component.LifeCycle
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.test.spock.UseGuiceyApp
import spock.lang.Specification

/**
 * @author Vyacheslav Rusakov
 * @since 31.07.2016
 */
@UseGuiceyApp(App)
class LifecycleStartedForGuiceyTest extends Specification {

    static boolean called

    def "Check lifecycle started for lightweight guicey test"() {

        expect:
        called

    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder().build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
            environment.lifecycle().addEventListener(new LifeCycle.Listener() {
                @Override
                void lifeCycleStarted(LifeCycle event) {
                    called = true
                }
            })
        }
    }
}