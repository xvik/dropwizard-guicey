package ru.vyarus.guicey.gsp.views

import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp
import ru.vyarus.guicey.gsp.ServerPagesBundle
import ru.vyarus.guicey.gsp.info.GspInfoService
import spock.lang.Specification

import jakarta.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 05.06.2019
 */
@TestDropwizardApp(value = App)
class ViewConfigurationModificationTest extends Specification {

    @Inject
    GspInfoService info

    def "Check views configuration modification in app"() {

        expect: "application started without errors"
        def config = info.getViewsConfig()
        config['freemarker']['cache_storage'] == 'yes'
        config['test'].isEmpty()
    }

    static class App extends Application<Configuration> {

        ServerPagesBundle bundle

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bundle = ServerPagesBundle.builder()
                    .printViewsConfiguration()
                    .build()
            bootstrap.addBundle(GuiceBundle.builder()
                    .bundles(
                            bundle,
                            ServerPagesBundle.app("app", "/app", "/app")
                                    .viewsConfigurationModifier('freemarker', { it['cache_storage'] = "yes" })
                                    .viewsConfigurationModifier('test', {})
                                    .build())
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
            assert bundle.getViewsConfig()['freemarker']['cache_storage'] == 'yes'
            assert bundle.getViewsConfig()['test'].isEmpty()
        }
    }
}
