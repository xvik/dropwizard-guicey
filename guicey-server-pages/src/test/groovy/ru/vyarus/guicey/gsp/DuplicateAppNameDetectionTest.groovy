package ru.vyarus.guicey.gsp

import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.test.TestSupport
import spock.lang.Specification

/**
 * @author Vyacheslav Rusakov
 * @since 06.02.2019
 */
class DuplicateAppNameDetectionTest extends Specification {

    def "Check app collision detection"() {

        when: "starting app"
        TestSupport.runWebApp(AppInit)
        then: "duplicate name error"
        def ex = thrown(IllegalArgumentException)
        ex.message == 'Server pages application with name \'app\' is already registered'
    }

    static class AppInit extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .bundles(
                            ServerPagesBundle.builder().build(),
                            ServerPagesBundle.app("app", "/app", "/app")
                                    .indexPage("index.html")
                                    .build(),
                            ServerPagesBundle.app("app", "/app", "/app")
                                    .indexPage("index.html")
                                    .build())
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }
}
