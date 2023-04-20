package ru.vyarus.guicey.gsp.views

import com.fasterxml.jackson.annotation.JsonProperty
import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp
import ru.vyarus.guicey.gsp.AbstractTest
import ru.vyarus.guicey.gsp.ServerPagesBundle

/**
 * @author Vyacheslav Rusakov
 * @since 26.01.2019
 */
@TestDropwizardApp(value = App, config = 'src/test/resources/views.yml')
class ViewsConfigurationTest extends AbstractTest {

    def "Check views configuration binding"() {

        when: "accessing direct template"
        def res = getHtml("/template.ftl")
        then: "rendered template"
        res.contains("page: /template.ftl")
    }

    static class App extends Application<Config> {

        ServerPagesBundle bundle

        @Override
        void initialize(Bootstrap<Config> bootstrap) {
            bundle = ServerPagesBundle.builder()
                    .viewsConfiguration({ it.views })
            // used to assert global config binding
                    .viewsConfigurationModifier('freemarker', { assert it['cache_storage'] != null })
                    .printViewsConfiguration()
                    .build()
            bootstrap.addBundle(GuiceBundle.builder()
                    .bundles(
                            bundle,
                            ServerPagesBundle.app("app", "/app", "/").build())
                    .build())
        }

        @Override
        void run(Config configuration, Environment environment) throws Exception {
            assert bundle.getRenderers().size() == 2
            assert bundle.getViewsConfig() != null
            // value from yaml
            assert bundle.getViewsConfig()['freemarker']['cache_storage'] == 'freemarker.cache.NullCacheStorage'
        }
    }

    static class Config extends Configuration {

        @JsonProperty
        Map<String, Map<String, String>> views;

    }
}
