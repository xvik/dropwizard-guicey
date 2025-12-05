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
class RendererDetectionTest extends Specification {

    def "Check renderer requirement check"() {

        when: "starting app"
        TestSupport.runWebApp(App)
        then: "absent renderer detected"
        def ex = thrown(IllegalStateException)
        ex.message == 'Required template engines are missed for server pages application \'app\': fooo (available engines: freemarker, mustache)'
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .bundles(
                            ServerPagesBundle.app("app", "/app", "/app")
                                    .requireRenderers("fooo")
                                    .build())
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }
}
