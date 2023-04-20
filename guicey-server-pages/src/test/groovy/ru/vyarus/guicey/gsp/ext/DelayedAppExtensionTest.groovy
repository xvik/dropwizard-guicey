package ru.vyarus.guicey.gsp.ext

import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp
import ru.vyarus.guicey.gsp.AbstractTest
import ru.vyarus.guicey.gsp.ServerPagesBundle
import ru.vyarus.guicey.gsp.info.GspInfoService
import ru.vyarus.guicey.gsp.support.app.OverridableTemplateResource
import ru.vyarus.guicey.gsp.support.app.SubTemplateResource

import javax.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 29.11.2019
 */
@TestDropwizardApp(value = App, restMapping = "/rest/*")
class DelayedAppExtensionTest extends AbstractTest {

    @Inject
    GspInfoService info

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

        when: "accessing path"
        res = getHtml("/sample")
        then: "index page"
        res.contains("page: /sample")

        when: "accessing sub mapped path"
        res = getHtml("/sub/sample")
        then: "index page"
        res.contains("page: /sub/sample")

        and: "mapping correct"
        info.getApplication("app").getViewPaths().collect { it.mappedUrl } as Set == [
                "/sub/sample",
                "/sample"] as Set

        info.getApplication("app").getHiddenViewPaths().collect { it.mappedUrl } as Set == [
                "/sub/{name}"] as Set
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .extensions(OverridableTemplateResource, SubTemplateResource)
                    .bundles(
                            ServerPagesBundle.builder().build(),
                            ServerPagesBundle.app("app", "/app", "/")
                                    .indexPage("index.html")
                                    .build(),
                            ServerPagesBundle.extendApp("app")
                                    .delayedConfiguration({ env, assets, views ->
                                        assert env
                                        assert assets
                                        assert views
                                        assets.attach("/ext")
                                        views.map("/sub", "/sub")
                                    })
                                    .build())
                    .build())

        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }
}
