package ru.vyarus.dropwizard.guice.yaml.report

import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import ru.vyarus.dropwizard.guice.GuiceBundle
import ru.vyarus.dropwizard.guice.module.yaml.ConfigurationTree
import ru.vyarus.dropwizard.guice.module.yaml.report.BindingsConfig
import ru.vyarus.dropwizard.guice.module.yaml.report.ConfigBindingsRenderer
import ru.vyarus.dropwizard.guice.test.spock.UseGuiceyApp
import spock.lang.Specification

import javax.inject.Inject


/**
 * @author Vyacheslav Rusakov
 * @since 14.06.2018
 */
@UseGuiceyApp(App)
class EmptyReportRenderTest extends Specification {

    @Inject
    ConfigurationTree tree

    def "Check empty report"() {
        expect:
        render(new BindingsConfig()
                .showCustomConfigOnly()
                .showConfigurationTree()) == ""
    }

    private String render(BindingsConfig config) {
        new ConfigBindingsRenderer(tree).renderReport(config)
                .replaceAll("\r", "")
                .replaceAll(" +\n", "\n")
                .replaceAll('@[^]C \n]+', '@1111111')
    }

    static class App extends Application<Configuration> {
        @Override
        void initialize(Bootstrap<Configuration> bootstrap) {
            bootstrap.addBundle(GuiceBundle.builder().build())
        }

        @Override
        void run(Configuration configuration, Environment environment) throws Exception {

        }
    }
}