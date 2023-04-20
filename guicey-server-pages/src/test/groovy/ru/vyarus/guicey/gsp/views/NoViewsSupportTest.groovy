package ru.vyarus.guicey.gsp.views

import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
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
        TestSupport.runWebApp(App, null)
        then: "no views support detected"
        def ex = thrown(IllegalStateException)
        ex.message == 'Either server pages support bundle was not installed (use ServerPagesBundle.builder() to create bundle)  or it was installed after \'app\' application bundle'

    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {


            bootstrap.addBundle(GuiceBundle.builder()
                    .bundles(
                            // NO global setup
                            ServerPagesBundle.app("app", "/app", "/").build())
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }
}
