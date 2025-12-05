package ru.vyarus.guicey.gsp.cases

import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp
import ru.vyarus.guicey.gsp.AbstractTest
import ru.vyarus.guicey.gsp.ServerPagesBundle
import ru.vyarus.guicey.gsp.views.template.Template

import jakarta.ws.rs.GET
import jakarta.ws.rs.Path

/**
 * @author Vyacheslav Rusakov
 * @since 12.10.2020
 */
@TestDropwizardApp(value = App, restMapping = "/rest/*")
class ViewRestDetectionTest extends AbstractTest {

    def "Check view assigned"() {

        when: "accessing view"
        String res = get("sample")
        then: "view loaded"
        res == 'something'
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .bundles(
                            ServerPagesBundle.app("app", "/app", "/")
                                    .mapViews('/views/prefix/')
                                    .build())
                    .extensions(SampleRest)
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }


    // no leading slash!
    @Path('views/prefix')
    @Template
    static class SampleRest {

        @GET
        @Path("sample")
        String get() {
            return 'something'
        }
    }
}
