package ru.vyarus.guicey.gsp

import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp
import ru.vyarus.guicey.gsp.info.GspInfoService
import ru.vyarus.guicey.gsp.support.app.SampleTemplateResource

import javax.inject.Inject

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
                            ServerPagesBundle.builder().build(),
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
