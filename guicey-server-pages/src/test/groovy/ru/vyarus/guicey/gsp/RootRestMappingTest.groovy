package ru.vyarus.guicey.gsp

import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp

/**
 * @author Vyacheslav Rusakov
 * @since 24.01.2019
 */
@TestDropwizardApp(value = App, restMapping = "/*",
        configOverride = "server.applicationContextPath: /prefix/")
class RootRestMappingTest extends AbstractTest {

    def "Check app mapped"() {

        when: "accessing app"
        String res = getHtml("/prefix/app/")
        then: "index page"
        res.contains("page: /")

        when: "accessing direct file"
        res = getHtml("/prefix/app/index.html")
        then: "index page"
        res.contains("Sample page")

        when: "accessing direct template"
        res = getHtml("/prefix/app/template.ftl")
        then: "rendered template"
        res.contains("page: /prefix/app/template.ftl")
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .bundles(
                            ServerPagesBundle.builder().build(),
                            ServerPagesBundle.app("app", "/app", "/app")
                                    .indexPage("template.ftl")
                                    .build())
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }
}
