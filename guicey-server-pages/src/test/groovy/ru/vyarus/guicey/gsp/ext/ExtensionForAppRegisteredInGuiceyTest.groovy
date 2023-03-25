package ru.vyarus.guicey.gsp.ext

import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp
import ru.vyarus.guicey.gsp.AbstractTest
import ru.vyarus.guicey.gsp.ServerPagesBundle

/**
 * @author Vyacheslav Rusakov
 * @since 06.02.2019
 */
@TestDropwizardApp(value = App, config = 'src/test/resources/conf.yml')
class ExtensionForAppRegisteredInGuiceyTest extends AbstractTest {

    def "Check app mapped"() {

        when: "accessing app"
        String res = getHtml("/")
        then: "index page"
        res.contains("overridden sample page")

        when: "accessing direct file"
        res = getHtml("/index.html")
        then: "index page"
        res.contains("overridden sample page")

        when: "accessing resource"
        res = get("/css/style.css")
        then: "css"
        res.contains("/* sample page css */")

        when: "accessing direct template"
        res = getHtml("/template.ftl")
        then: "rendered template"
        res.contains("page: /template.ftl")

        when: "accessing direct ext template"
        res = getHtml("/ext.ftl")
        then: "rendered template"
        res.contains("ext template")
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .bundles(
                            ServerPagesBundle.builder().build(),
                            // extension registered before application
                            ServerPagesBundle.extendApp("app")
                                    .attachAssets("/ext")
                                    .build(),
                            ServerPagesBundle.app("app", "/app", "/")
                                    .indexPage("index.html")
                                    .build())
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }
}
