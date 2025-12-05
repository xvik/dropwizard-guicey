package ru.vyarus.guicey.gsp.views

import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp
import ru.vyarus.guicey.gsp.AbstractTest
import ru.vyarus.guicey.gsp.ServerPagesBundle
import ru.vyarus.guicey.gsp.info.GspInfoService
import ru.vyarus.guicey.gsp.support.app.clash.BaseViewResource

import jakarta.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 05.12.2019
 */
@TestDropwizardApp(value = App, restMapping = "/rest/*")
class ClashingDefaultHandlersMappingTest extends AbstractTest {

    @Inject
    GspInfoService info

    def "Check direct template in sub mapping"() {

        when: "accessing base mapping"
        String res = getHtml("/app/one")
        then: "index page"
        res.contains("page: /app/one")

        when: "accessing base mapping, clashing with direct template from other sub mapping"
        res = getHtml("/app/bar/two")
        then: "index page"
        res.contains("page: /app/bar/two")
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .extensions(BaseViewResource)
                    .bundles(
                            ServerPagesBundle.app("app", "app", "/app")
                                    .mapViews("/foo/")
                                    .mapViews("/bar", "/foo/bar/")
                                    .build())
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }
}
