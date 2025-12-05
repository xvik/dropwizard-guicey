package ru.vyarus.guicey.gsp

import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp
import ru.vyarus.guicey.gsp.info.GspInfoService
import ru.vyarus.guicey.gsp.support.app.SampleTemplateResource

import jakarta.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 14.01.2019
 */
@TestDropwizardApp(value = App, restMapping = "/rest/*")
class ResourceMappingTest extends AbstractTest {

    @Inject
    GspInfoService info

    def "Check custom resource mapping"() {

        when: "accessing template through resource"
        String res = getHtml("/sample/tt")
        then: "template mapped"
        res.contains("name: tt")

        and: "recognized mappings"
        info.getApplication("app").getViewPaths().collect { it.mappedUrl } as Set == [
                "/sample/error",
                "/sample/error2",
                "/sample/notfound",
                "/sample/{name}"] as Set

    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .extensions(SampleTemplateResource)
                    .bundles(
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
