package ru.vyarus.guicey.gsp.info

import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import io.dropwizard.views.View
import io.dropwizard.views.ViewRenderer
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.test.jupiter.TestDropwizardApp
import ru.vyarus.guicey.gsp.ServerPagesBundle
import spock.lang.Specification

import javax.inject.Inject

/**
 * @author Vyacheslav Rusakov
 * @since 04.12.2019
 */
@TestDropwizardApp(value = App)
class ViewsInfoTest extends Specification {

    @Inject
    GspInfoService info

    def "Check views info"() {

        expect: "information correct"
        with(info.getViewsConfig()) {
            size() == 1
            it["foo"].size() == 1
            it["foo"]["some"] == "bar"
        }
        info.getViewRendererNames() as Set == ["freemarker", "mustache", "foo"] as Set
        info.getViewRenderers().size() == 3
    }

    static class App extends Application<Configuration> {

        ServerPagesBundle bundle

        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bundle =
                    bootstrap.addBundle(GuiceBundle.builder()
                            .bundles(
                                    ServerPagesBundle.builder()
                                            .addViewRenderers(new ViewRenderer() {
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
                                                    return "foo"
                                                }
                                            })
                                            .viewsConfiguration({ ['foo': ['some': 'bar']] })
                                            .printViewsConfiguration()
                                            .build())
                            .build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {
        }
    }
}
