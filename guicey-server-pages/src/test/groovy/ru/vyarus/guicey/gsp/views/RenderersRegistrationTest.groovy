package ru.vyarus.guicey.gsp.views

import io.dropwizard.core.Application
import io.dropwizard.core.Configuration
import io.dropwizard.core.setup.Bootstrap
import io.dropwizard.core.setup.Environment
import io.dropwizard.views.common.View
import io.dropwizard.views.common.ViewRenderer
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp
import ru.vyarus.guicey.gsp.ServerPagesBundle
import spock.lang.Specification

/**
 * @author Vyacheslav Rusakov
 * @since 26.01.2019
 */
@TestDropwizardApp(value = App, restMapping = "/rest/*")
class RenderersRegistrationTest extends Specification {

    def "Check renderers registration"() {

        expect: "duplicate renderer removed"
        true
    }

    static class App extends Application<Configuration> {

        ServerPagesBundle bundle

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bundle = ServerPagesBundle.builder()
                    .addViewRenderers(
                            new CustomRenderer("r1"), new CustomRenderer("r2"),
                            new CustomRenderer("r2"), new CustomRenderer("r3"))
                    .printViewsConfiguration()
                    .build()
            bootstrap.addBundle(GuiceBundle.builder()
                    .bundles(
                            bundle,
                            ServerPagesBundle.app("app", "/app", "/").build(),
                            ServerPagesBundle.app("app2", "/app", "/2").build())
                    .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
            assert bundle.getRenderers().collect {
                it.getConfigurationKey()
            } as Set == ['freemarker', 'mustache', 'r1', 'r2', 'r3'] as Set
        }
    }

    static class CustomRenderer implements ViewRenderer {

        String key

        CustomRenderer(String key) {
            this.key = key
        }

        @Override
        boolean isRenderable(View view) {
            return false
        }

        @Override
        void render(View view, Locale locale, OutputStream output) throws IOException {

        }

        @Override
        void configure(Map<String, String> options) {

        }

        @Override
        String getConfigurationKey() {
            return key
        }
    }

}
