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
import ru.vyarus.guicey.gsp.info.model.GspApp
import ru.vyarus.guicey.gsp.support.app.OverridableTemplateResource
import ru.vyarus.guicey.gsp.support.app.SubTemplateResource

import javax.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 02.12.2019
 */
@TestDropwizardApp(value = App, restMapping = "/rest/*")
class ViewsSubMappingTest extends AbstractTest {

    @Inject
    GspInfoService info

    def "Check app mapped"() {

        when: "accessing path"
        String res = getHtml("/app/sample")
        then: "index page"
        res.contains("page: /app/sample")

        when: "accessing sub mapped path"
        res = getHtml("/app/sub/sample")
        then: "index page"
        res.contains("page: /app/sub/sample")

        when: "get info"
        GspApp app = info.getApplication("app")
        then: "info correct"
        with(app.getViews()) {
            size() == 2
            it[""] == "app/"
            it["sub/"] == "sub/"
        }
        with(app.getAssets()) {
            size() == 1
            it.get("") == ["app/"]
        }
        app.indexFile == "index.html"

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
                            ServerPagesBundle.app("app", "/app", "/app")
                                    .mapViews("app")
                                    .mapViews("/sub/", "/sub/")
                                    .indexPage("index.html")
                                    .build())
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }
}
