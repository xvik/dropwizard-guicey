package ru.vyarus.guicey.gsp

import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp

/**
 * @author Vyacheslav Rusakov
 * @since 24.03.2020
 */
@TestDropwizardApp(value = App, restMapping = "/rest/*")
class NonRootIndexTemplateTest extends AbstractTest {


    def "Check non root mapping support"() {

        when: "accessing root with trailing slash"
        String res = getHtml("/sub/")
        then: "index page"
        res.contains("page: /sub/")

        when: "accessing root without trailing slash"
        res = getHtml("/sub")
        then: "index page"
        res.contains("page: /sub")
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .bundles(
                            ServerPagesBundle.builder().build(),
                            ServerPagesBundle.app("app", "/app", "/sub")
                                    .indexPage("template.ftl")
                                    .build())
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }
}
