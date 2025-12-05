package ru.vyarus.guicey.gsp.views

import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.test.TestSupport
import ru.vyarus.guicey.gsp.ServerPagesBundle
import spock.lang.Specification

/**
 * @author Vyacheslav Rusakov
 * @since 05.06.2019
 */
class NoViewsSupportTest extends Specification {

    def "Check view support absence detection"() {

        when: "starting app"
        TestSupport.runWebApp(App)
        then: "all ok - global bundle is optioal"
        true
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {


            bootstrap.addBundle(GuiceBundle.builder()
                    .bundles(
                            // NO global setup
                            ServerPagesBundle.app("app", "/app", "/app").build())
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }
}
