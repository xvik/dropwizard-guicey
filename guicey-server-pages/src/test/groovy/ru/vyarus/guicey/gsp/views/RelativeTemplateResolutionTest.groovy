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
import ru.vyarus.guicey.gsp.support.relative.RelativeTemplateResource

import jakarta.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 27.01.2019
 */
@TestDropwizardApp(value = App, restMapping = "/rest/*")
class RelativeTemplateResolutionTest extends AbstractTest {

    @Inject
    GspInfoService info

    def "Check relative templates"() {

        when: "template from annotation"
        String res = getHtml("/relative/direct")
        then: "found"
        res.contains("name: app")

        when: "template relative to class"
        res = getHtml("/relative/relative")
        then: "found"
        res.contains("root name: app")

        when: "template relative to dir"
        res = getHtml("/relative/dir")
        then: "found"
        res.contains("page: /relative/dir")

        and: "mapping correct"
        info.getApplication("app").getViewPaths().collect { it.mappedUrl } as Set == [
                "/relative/dir",
                "/relative/direct",
                "/relative/relative"] as Set

        info.getApplication("app").getHiddenViewPaths().isEmpty()
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .bundles(
                            ServerPagesBundle.builder().build(),
                            ServerPagesBundle.app("app", "/app", "/")
                                    .build())
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
            environment.jersey().register(RelativeTemplateResource)
        }
    }
}
