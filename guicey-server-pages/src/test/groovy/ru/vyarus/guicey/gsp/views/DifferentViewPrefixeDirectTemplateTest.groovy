package ru.vyarus.guicey.gsp.views

import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp
import ru.vyarus.guicey.gsp.AbstractTest
import ru.vyarus.guicey.gsp.ServerPagesBundle
import ru.vyarus.guicey.gsp.support.app.OverridableTemplateResource
import ru.vyarus.guicey.gsp.support.app.SubTemplateResource

/**
 * @author Vyacheslav Rusakov
 * @since 03.12.2019
 */
@TestDropwizardApp(value = App, restMapping = "/rest/*")
class DifferentViewPrefixeDirectTemplateTest extends AbstractTest {

    def "Check direct template in sub mapping"() {

        when: "accessing direct template"
        String res = getHtml("/app/template.ftl")
        then: "index page"
        res.contains("page: /app/template.ftl")

        when: "accessing sub mapping direct template"
        res = getHtml("/app/sub/subtemplate.ftl")
        then: "index page"
        res.contains("page: /app/sub/subtemplate.ftl subcontext: /sub/")
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .extensions(OverridableTemplateResource, SubTemplateResource)
                    .bundles(
                            ServerPagesBundle.builder().build(),
                            ServerPagesBundle.app("app", "/app", "/app")
                                    .mapViews("app")
                                    .mapViews("/sub", "/sub")
                                    .attachAssets("/sub", "/app/sub")
                                    .indexPage("index.html")
                                    .build())
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }
}
