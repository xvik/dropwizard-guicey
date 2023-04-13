package ru.vyarus.guicey.gsp.info

import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp
import ru.vyarus.guicey.gsp.ServerPagesBundle
import ru.vyarus.guicey.gsp.support.relative.RelativeTemplateResource
import spock.lang.Specification

import jakarta.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 04.12.2019
 */
@TestDropwizardApp(App)
class ApplicationInfoTest extends Specification {

    @Inject
    GspInfoService info

    def "Check app info correctness"() {

        expect:
        info.getApplications().size() == 2

        and: "app - mostly default mappings, but with extensions"
        with(info.getApplication("app")) {
            name == "app"
            mainContext
            mappingUrl == "/app/"
            rootUrl == "/app/"
            requiredRenderers == ["freemarker"]

            mainAssetsLocation == "app/"
            assetExtensions.size() == 1
            assetExtensions.get("") as List == ["foo/"] as List
            viewExtensions.size() == 1
            viewExtensions.get("foo/") == "foo/"
            with(assets) {
                keySet().size() == 1
                it.get("") as List == ["foo/", "app/"] as List
            }
            with(views) {
                keySet().size() == 2
                it[""] == "app/"
                it["foo/"] == "foo/"
            }
            mainRestPrefix == "app/"
            restRootUrl == "/"

            indexFile == ""
            filesRegex != null
            hasDefaultFilesRegex

            !spa
            spaRegex != null
            hasDefaultSpaRegex

            errorPages.size() == 1
            errorPages[404] == "err.tpl"
            defaultErrorPage == null

            hiddenViewPaths.isEmpty()
            viewPaths.size() == 3
        }

        and: "app2 - customized mappings"
        with(info.getApplication("app2")) {
            name == "app2"
            !mainContext
            mappingUrl == "/app2/"
            rootUrl == "/app2/"
            requiredRenderers.isEmpty()

            mainAssetsLocation == "app/"
            assetExtensions.isEmpty()
            viewExtensions.isEmpty()
            with(assets) {
                keySet().size() == 2
                it.get("") as List == ["foo/bar/", "app/"] as List
                it.get("some/") as List == ["bazz/"] as List
            }
            with(views) {
                keySet().size() == 2
                it[""] == "app/"
                it["some/"] == "bazz/"
            }
            mainRestPrefix == "app/"
            restRootUrl == "/"

            indexFile == "sample.html"
            filesRegex == "someregex"
            !hasDefaultFilesRegex

            spa
            spaRegex == "otherregex"
            !hasDefaultSpaRegex

            errorPages.size() == 1
            errorPages[-1] == "err.tpl"
            defaultErrorPage == "err.tpl"

            hiddenViewPaths.isEmpty()
            viewPaths.size() == 3
        }

    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .bundles(
                            ServerPagesBundle.builder().build(),
                            ServerPagesBundle.app("app", "app", "/app")
                                    .errorPage(404, "err.tpl")
                                    .requireRenderers("freemarker")
                                    .build(),
                            ServerPagesBundle.adminApp("app2", "app", "/app2")
                                    .indexPage("sample.html")
                                    .attachAssets("foo.bar")
                                    .attachAssets("/some", "bazz")
                                    .mapViews("app")
                                    .mapViews("/some", "/bazz")
                                    .errorPage("err.tpl")
                                    .filePattern("someregex")
                                    .spaRouting("otherregex")
                                    .build(),
                            ServerPagesBundle.extendApp("app")
                                    .mapViews("/foo", "foo")
                                    .attachAssets("foo")
                                    .build())
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
            environment.jersey().register(RelativeTemplateResource)
        }
    }
}
