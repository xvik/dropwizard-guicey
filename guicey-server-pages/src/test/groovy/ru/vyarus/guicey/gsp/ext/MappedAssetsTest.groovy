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
 * @since 29.11.2019
 */
@TestDropwizardApp(value = App, config = 'src/test/resources/conf.yml')
class MappedAssetsTest extends AbstractTest {

    def "Check assets mapped"() {

        when: "accessing mapped url"
        String res = getHtml("/sample/ext.ftl")
        then: "rendered template"
        res.contains("ext template")
    }

    static class App extends Application<Configuration> {

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder()
                    .bundles(
                            ServerPagesBundle.app("app", "/app", "/")
                                    .indexPage("index.html")
                                    .attachAssets('/sample', 'ext')
                                    .build())
                    .build())

        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }
}
