package ru.vyarus.guicey.gsp.views

import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp
import ru.vyarus.guicey.gsp.ServerPagesBundle
import spock.lang.Specification

/**
 * @author Vyacheslav Rusakov
 * @since 07.02.2019
 */
@TestDropwizardApp(value = App, restMapping = "/rest/*")
class ViewsConfigCreationTest extends Specification {

    def "Check null views configuration binding"() {

        expect: "created main config map and sub map for freemarker"
        true
    }

    static class App extends Application<Configuration> {

        ServerPagesBundle bundle

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bundle = ServerPagesBundle.builder()
                    .viewsConfiguration({ null })
                    .viewsConfigurationModifier('freemarker', { it['foo'] = 'bar' })
                    .printViewsConfiguration()
                    .build()
            bootstrap.addBundle(GuiceBundle.builder()
                    .bundles(
                            bundle,
                            ServerPagesBundle.app("app", "/app", "/").build())
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
            assert bundle.getViewsConfig()['freemarker']['foo'] == 'bar'
        }
    }
}
